package org.example.tnal_youth_backend.authentication.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Telegram calls this directly, for every message sent to the bot, once
 * {@code https://api.telegram.org/bot<token>/setWebhook} has been pointed at
 * this endpoint's public URL. Requires a real, publicly reachable HTTPS
 * origin — Telegram cannot reach {@code localhost}, so this path only
 * matters once the backend is actually deployed somewhere with a public
 * URL. For local development, {@link TelegramPollingScheduler} reaches the
 * exact same {@link TelegramUpdateHandler} without needing this endpoint at
 * all — the two are mutually exclusive at the Telegram API level (Telegram
 * refuses {@code getUpdates} polling while a webhook is registered), so only
 * turn this on in an environment where you've actually called
 * {@code setWebhook}, and turn polling off there via
 * {@code app.telegram.polling-enabled=false}.
 *
 * <p>Authenticated by Telegram's own {@code secret_token} mechanism: the
 * same value passed as {@code secret_token} to {@code setWebhook} is echoed
 * back on every call as the {@code X-Telegram-Bot-Api-Secret-Token} header,
 * so a mismatch means the request didn't originate from Telegram.
 */
@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramUpdateHandler telegramUpdateHandler;

    @Value("${app.telegram.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody TelegramUpdate update
    ) {
        if (!secretMatches(webhookSecret, secretToken)) {
            log.warn("TelegramWebhookController: rejected call with invalid or missing secret token");
            return ResponseEntity.status(401).build();
        }

        telegramUpdateHandler.handle(update);

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
