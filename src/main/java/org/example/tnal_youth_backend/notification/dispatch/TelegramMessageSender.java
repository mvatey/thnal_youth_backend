package org.example.tnal_youth_backend.notification.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sends a notification's title/body as a Telegram message to a linked
 * {@code users.telegram_chat_id} via the Bot API's {@code sendMessage}
 * endpoint (plain REST call — this is NOT where the bot's own chat logic
 * lives; that's on the user's separately-hosted chatbot server, which only
 * calls INTO this backend via {@code POST /api/telegram/link}).
 */
@Component
@Slf4j
public class TelegramMessageSender {

    private static final String SEND_MESSAGE_URL_TEMPLATE =
            "https://api.telegram.org/bot%s/sendMessage";

    private final RestTemplate restTemplate;

    public TelegramMessageSender(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Left blank by default on purpose — the user fills in the real bot
     * token from @BotFather directly in
     * {@code application.properties}/env; it must never be embedded here.
     */
    @Value("${app.telegram.bot-token:}")
    private String botToken;

    @Value("${app.notification.base-url:}")
    private String baseUrl;

    public void send(User user, NotificationModel notification) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException(
                    "Telegram delivery is not configured"
            );
        }

        String url = String.format(SEND_MESSAGE_URL_TEMPLATE, botToken);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", user.getTelegramChatId());
        body.put("text", buildText(notification));
        body.put("parse_mode", "Markdown");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    private String buildText(NotificationModel notification) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(escapeMarkdown(notification.getTitle())).append("*");

        if (notification.getBody() != null && !notification.getBody().isBlank()) {
            sb.append("\n").append(escapeMarkdown(notification.getBody()));
        }

        String link = resolveAbsoluteActionUrl(notification.getActionUrl());
        if (link != null) {
            sb.append("\n").append(link);
        }

        return sb.toString();
    }

    private String resolveAbsoluteActionUrl(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) {
            return null;
        }

        if (actionUrl.startsWith("http://") || actionUrl.startsWith("https://")) {
            return actionUrl;
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }

        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        String normalizedPath = actionUrl.startsWith("/") ? actionUrl : "/" + actionUrl;

        return normalizedBase + normalizedPath;
    }

    /**
     * Escapes Telegram's Markdown (legacy, not MarkdownV2) special
     * characters that would otherwise break parsing — {@code _*[`} — since
     * notification titles/bodies are free text and can legitimately
     * contain any of them.
     */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`");
    }
}
