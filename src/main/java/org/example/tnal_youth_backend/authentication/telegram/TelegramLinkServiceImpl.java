package org.example.tnal_youth_backend.authentication.telegram;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.TelegramLinkToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.TelegramLinkTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TelegramLinkServiceImpl implements TelegramLinkService {

    /** Matches the 30-day TTL used for the analogous password-reset-style tokens elsewhere. */
    private static final long TOKEN_TTL_DAYS = 30;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final TelegramLinkTokenRepository telegramLinkTokenRepository;
    private final UserRepository userRepository;

    /**
     * Left blank by default on purpose — see the notification feature setup
     * notes for exactly which {@code application.properties} keys need a
     * real value filled in.
     */
    @Value("${app.telegram.bot-username:}")
    private String botUsername;

    @Override
    @Transactional
    public TelegramConnectInfoResponse getConnectInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "User " + userId + " not found",
                        HttpStatus.NOT_FOUND
                ));

        if (user.getTelegramChatId() != null) {
            return TelegramConnectInfoResponse.builder()
                    .connected(true)
                    .deepLink(null)
                    .build();
        }

        // Standalone accounts (ADMIN, VIEWER, or any role created without a
        // linked member record) never receive a notification through any
        // channel — every notification-creation call site resolves
        // recipients via the member link (findByMemberId or a join through
        // members), never a bare user id. No point minting a token or
        // showing the connect-Telegram reminder for an account that could
        // never actually use it.
        if (user.getMemberId() == null) {
            return TelegramConnectInfoResponse.builder()
                    .connected(false)
                    .deepLink(null)
                    .build();
        }

        String token = getOrCreateLinkToken(user);

        return TelegramConnectInfoResponse.builder()
                .connected(false)
                .deepLink(buildDeepLink(token))
                .build();
    }

    private String getOrCreateLinkToken(User user) {
        OffsetDateTime now = OffsetDateTime.now();

        return telegramLinkTokenRepository
                .findTopByUser_IdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        user.getId(),
                        now
                )
                .map(TelegramLinkToken::getToken)
                .orElseGet(() -> {
                    TelegramLinkToken created = TelegramLinkToken.builder()
                            .user(user)
                            .token(generateToken())
                            .expiresAt(now.plusDays(TOKEN_TTL_DAYS))
                            .build();

                    telegramLinkTokenRepository.save(created);
                    return created.getToken();
                });
    }

    private String buildDeepLink(String token) {
        if (botUsername == null || botUsername.isBlank()) {
            // Config not wired up yet — surface null rather than a broken
            // https://t.me/?start=... link the banner would render as-is.
            return null;
        }

        return "https://t.me/" + botUsername + "?start=" + token;
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    @Transactional
    public void confirmLink(String token, Long chatId) {
        TelegramLinkToken linkToken = telegramLinkTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        "TELEGRAM_LINK_TOKEN_NOT_FOUND",
                        "Unknown or already-used Telegram link token",
                        HttpStatus.NOT_FOUND
                ));

        if (linkToken.getConsumedAt() != null) {
            throw new BusinessException(
                    "TELEGRAM_LINK_TOKEN_CONSUMED",
                    "This Telegram link token has already been used",
                    HttpStatus.CONFLICT
            );
        }

        if (linkToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(
                    "TELEGRAM_LINK_TOKEN_EXPIRED",
                    "This Telegram link token has expired",
                    HttpStatus.GONE
            );
        }

        // A chat id already linked to a DIFFERENT user must not be silently
        // re-pointed — that would let one Telegram account impersonate two
        // member accounts' real-time notifications.
        userRepository.findByTelegramChatId(chatId)
                .filter(existingOwner -> !existingOwner.getId().equals(linkToken.getUser().getId()))
                .ifPresent(existingOwner -> {
                    throw new BusinessException(
                            "TELEGRAM_CHAT_ALREADY_LINKED",
                            "This Telegram account is already linked to a different member",
                            HttpStatus.CONFLICT
                    );
                });

        User user = linkToken.getUser();
        user.setTelegramChatId(chatId);
        user.setTelegramLinkedAt(OffsetDateTime.now());
        userRepository.save(user);

        linkToken.setConsumedAt(OffsetDateTime.now());
        telegramLinkTokenRepository.save(linkToken);
    }
}
