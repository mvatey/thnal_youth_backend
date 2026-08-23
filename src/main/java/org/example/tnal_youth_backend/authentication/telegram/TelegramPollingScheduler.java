package org.example.tnal_youth_backend.authentication.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.notification.dispatch.TelegramMessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reaches {@link TelegramUpdateHandler} by polling Telegram's
 * {@code getUpdates} instead of a webhook — works from any machine,
 * including plain {@code localhost}, since it's this app calling out to
 * Telegram rather than the other way around. This is the path that makes
 * the connect-Telegram flow testable in local dev; production can switch to
 * {@link TelegramWebhookController} instead (set
 * {@code app.telegram.polling-enabled=false} once a real webhook is
 * registered — Telegram doesn't allow both at once).
 *
 * <p>{@code lastUpdateId} is kept in memory only. On restart, Telegram will
 * redeliver already-seen updates once; re-processing an already-consumed
 * {@code /start <token>} is harmless — {@code TelegramLinkService#confirmLink}
 * just rejects it as an already-used token, and {@link TelegramUpdateHandler}
 * replies accordingly instead of erroring.
 */
@Component
@Slf4j
public class TelegramPollingScheduler {

    private static final String GET_UPDATES_URL_TEMPLATE =
            "https://api.telegram.org/bot%s/getUpdates?timeout=0&offset=%d";

    private final TelegramUpdateHandler telegramUpdateHandler;
    private final TelegramMessageSender telegramMessageSender;
    private final RestTemplate restTemplate;
    private final AtomicLong lastUpdateId = new AtomicLong(0);

    public TelegramPollingScheduler(
            TelegramUpdateHandler telegramUpdateHandler,
            TelegramMessageSender telegramMessageSender,
            RestTemplateBuilder restTemplateBuilder
    ) {
        this.telegramUpdateHandler = telegramUpdateHandler;
        this.telegramMessageSender = telegramMessageSender;
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Value("${app.telegram.polling-enabled:true}")
    private boolean pollingEnabled;

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        if (!pollingEnabled) {
            return;
        }

        String botToken = telegramMessageSender.getBotToken();

        if (botToken == null || botToken.isBlank()) {
            return;
        }

        String url = GET_UPDATES_URL_TEMPLATE.formatted(botToken, lastUpdateId.get() + 1);

        TelegramGetUpdatesResponse response;

        try {
            response = restTemplate.getForObject(url, TelegramGetUpdatesResponse.class);
        } catch (Exception e) {
            log.warn("TelegramPollingScheduler: getUpdates call failed", e);
            return;
        }

        List<TelegramUpdate> updates = response != null ? response.getResult() : null;

        if (updates == null || updates.isEmpty()) {
            return;
        }

        for (TelegramUpdate update : updates) {
            try {
                telegramUpdateHandler.handle(update);
            } catch (Exception e) {
                log.warn("TelegramPollingScheduler: failed to handle update {}", update.getUpdateId(), e);
            }

            if (update.getUpdateId() != null) {
                lastUpdateId.updateAndGet(previous -> Math.max(previous, update.getUpdateId()));
            }
        }
    }
}
