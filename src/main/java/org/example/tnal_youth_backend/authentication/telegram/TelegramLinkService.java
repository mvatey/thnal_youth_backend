package org.example.tnal_youth_backend.authentication.telegram;

public interface TelegramLinkService {

    /**
     * Returns whether the given user already has a linked Telegram chat,
     * and — when not linked — a ready-to-show {@code https://t.me/<bot>?start=<token>}
     * deep link, reusing an existing unexpired/unconsumed token when one
     * exists instead of minting a new one on every page load.
     */
    TelegramConnectInfoResponse getConnectInfo(Long userId);

    /**
     * Called from {@code POST /api/telegram/link} once the user's bot
     * server sees {@code /start <token>} and reports back the Telegram
     * chat id it should be linked to. Consumes the token and links it to
     * the token's owning user.
     */
    void confirmLink(String token, Long chatId);
}
