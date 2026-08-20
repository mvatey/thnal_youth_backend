package org.example.tnal_youth_backend.authentication.telegram;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * <p><b>NOTE for deployment:</b> {@code POST /api/telegram/link} is called by
 * the bot's own server, not a logged-in browser session, so it CANNOT sit
 * behind the normal JWT-authenticated rule set. It needs a
 * {@code permitAll()} entry in the security config. The dedicated
 * {@code X-Bot-Secret} header check below protects this one callback.
 */
@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramLinkController {

    private final TelegramLinkService telegramLinkService;

    /**
     * Left blank by default on purpose — the user fills in the real shared
     * secret (agreed with their bot server) directly in
     * {@code application.properties}/env; it must never be embedded here.
     */
    @Value("${app.telegram.webhook-secret:}")
    private String webhookSecret;

    @GetMapping("/connect-info")
    public ResponseEntity<TelegramConnectInfoResponse> connectInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(telegramLinkService.getConnectInfo(userId));
    }

    @PostMapping("/link")
    public ResponseEntity<Void> link(
            @RequestHeader(value = "X-Bot-Secret", required = false) String botSecret,
            @Valid @RequestBody TelegramLinkConfirmRequest request
    ) {
        if (!secretMatches(webhookSecret, botSecret)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid X-Bot-Secret"
            );
        }

        telegramLinkService.confirmLink(request.getToken(), request.getChatId());
        return ResponseEntity.ok().build();
    }

    private boolean secretMatches(String expected, String actual) {
        if (expected == null || expected.isBlank() || actual == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
