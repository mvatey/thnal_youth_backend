package org.example.tnal_youth_backend.notification.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.document.repository.DocumentRepository;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
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
 * endpoint (plain REST call). The bot's own inbound chat handling — reading
 * {@code /start} commands sent back by users — lives in
 * {@code TelegramUpdateHandler}, reached via either the webhook controller
 * or {@code TelegramPollingScheduler}, both in this same package.
 *
 * <p>Activity-invitation notifications get the richer bilingual formatted
 * message from {@link ActivityInvitationTelegramBuilder} (Telegram
 * {@code HTML} parse mode); every other notification type keeps the plain
 * Markdown message it already had.
 */
@Component
@Slf4j
public class TelegramMessageSender {

    private static final String SEND_MESSAGE_URL_TEMPLATE =
            "https://api.telegram.org/bot%s/sendMessage";

    private static final String ACTIVITY_INVITATION_TYPE_CODE = "ACTIVITY_INVITATION";
    private static final String ACTIVITY_UPDATED_TYPE_CODE = "ACTIVITY_UPDATED";
    private static final String ACTIVITY_CANCELLED_TYPE_CODE = "ACTIVITY_CANCELLED";
    private static final String CERTIFICATE_READY_TYPE_CODE = "ACTIVITY_CERTIFICATE_READY";
    private static final String DOCUMENT_ADDED_TYPE_CODE = "DOCUMENT_ADDED";
    private static final String ACTIVITY_REMINDER_TYPE_CODE = "ACTIVITY_REMINDER";

    private final RestTemplate restTemplate;
    private final ActivityRepository activityRepository;
    private final BranchRepository branchRepository;
    private final DocumentRepository documentRepository;
    private final ActivityInvitationTelegramBuilder activityInvitationTelegramBuilder;
    private final ActivityRescheduledTelegramBuilder activityRescheduledTelegramBuilder;
    private final ActivityCancelledTelegramBuilder activityCancelledTelegramBuilder;
    private final CertificateReadyTelegramBuilder certificateReadyTelegramBuilder;
    private final DocumentIssuedTelegramBuilder documentIssuedTelegramBuilder;
    private final ActivityReminderTelegramBuilder activityReminderTelegramBuilder;

    public TelegramMessageSender(
            RestTemplateBuilder restTemplateBuilder,
            ActivityRepository activityRepository,
            BranchRepository branchRepository,
            DocumentRepository documentRepository,
            ActivityInvitationTelegramBuilder activityInvitationTelegramBuilder,
            ActivityRescheduledTelegramBuilder activityRescheduledTelegramBuilder,
            ActivityCancelledTelegramBuilder activityCancelledTelegramBuilder,
            CertificateReadyTelegramBuilder certificateReadyTelegramBuilder,
            DocumentIssuedTelegramBuilder documentIssuedTelegramBuilder,
            ActivityReminderTelegramBuilder activityReminderTelegramBuilder
    ) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        this.activityRepository = activityRepository;
        this.branchRepository = branchRepository;
        this.documentRepository = documentRepository;
        this.activityInvitationTelegramBuilder = activityInvitationTelegramBuilder;
        this.activityRescheduledTelegramBuilder = activityRescheduledTelegramBuilder;
        this.activityCancelledTelegramBuilder = activityCancelledTelegramBuilder;
        this.certificateReadyTelegramBuilder = certificateReadyTelegramBuilder;
        this.documentIssuedTelegramBuilder = documentIssuedTelegramBuilder;
        this.activityReminderTelegramBuilder = activityReminderTelegramBuilder;
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
        String richText = buildRichText(user, notification);

        sendRaw(
                user.getTelegramChatId(),
                richText != null ? richText : buildText(notification),
                richText != null ? "HTML" : "Markdown"
        );
    }

    /**
     * @return the type-specific bilingual HTML message for activity
     * invitation/reschedule/cancellation notifications, or {@code null} for
     * every other type (or if the activity can no longer be found) -- the
     * caller falls back to the plain Markdown message instead.
     */
    private String buildRichText(User user, NotificationModel notification) {
        String typeCode = notification.getTypeCode();

        if (DOCUMENT_ADDED_TYPE_CODE.equals(typeCode)) {
            return notification.getDocumentId() == null
                    ? null
                    : buildDocumentIssuedText(notification.getDocumentId(), user.getFullNameKm());
        }

        if (notification.getActivityId() == null) {
            return null;
        }

        if (ACTIVITY_INVITATION_TYPE_CODE.equals(typeCode)) {
            return buildActivityInvitationText(notification.getActivityId(), user.getFullNameKm());
        }

        if (ACTIVITY_UPDATED_TYPE_CODE.equals(typeCode)) {
            return buildActivityRescheduledText(notification.getActivityId(), user.getFullNameKm());
        }

        if (ACTIVITY_CANCELLED_TYPE_CODE.equals(typeCode)) {
            return buildActivityCancelledText(notification.getActivityId(), user.getFullNameKm());
        }

        if (CERTIFICATE_READY_TYPE_CODE.equals(typeCode)) {
            return buildCertificateReadyText(notification.getActivityId(), user.getFullNameKm());
        }

        if (ACTIVITY_REMINDER_TYPE_CODE.equals(typeCode)) {
            return buildActivityReminderText(notification.getActivityId(), user.getFullNameKm());
        }

        return null;
    }

    /**
     * Low-level {@code sendMessage} call, shared by the notification
     * fan-out above and {@code TelegramUpdateHandler}'s replies to incoming
     * {@code /start} messages. {@code parseMode} may be {@code null} for
     * plain, unformatted text.
     */
    public void sendRaw(Long chatId, String text, String parseMode) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException(
                    "Telegram delivery is not configured"
            );
        }

        String url = String.format(SEND_MESSAGE_URL_TEMPLATE, botToken);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);

        if (parseMode != null) {
            body.put("parse_mode", parseMode);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    public String getBotToken() {
        return botToken;
    }

    /**
     * @return the formatted invitation text, or {@code null} if the
     * activity can no longer be found — the caller falls back to the plain
     * Markdown message instead of sending nothing.
     */
    private String buildActivityInvitationText(Long activityId, String recipientNameKm) {
        Activity activity = activityRepository.findById(activityId).orElse(null);

        if (activity == null) {
            return null;
        }

        Branch branch = branchRepository.findById(activity.getBranchId()).orElse(null);

        return activityInvitationTelegramBuilder.build(activity, branch, recipientNameKm);
    }

    private String buildActivityRescheduledText(Long activityId, String recipientNameKm) {
        Activity activity = activityRepository.findById(activityId).orElse(null);

        if (activity == null) {
            return null;
        }

        Branch branch = branchRepository.findById(activity.getBranchId()).orElse(null);

        return activityRescheduledTelegramBuilder.build(activity, branch, recipientNameKm);
    }

    private String buildActivityCancelledText(Long activityId, String recipientNameKm) {
        Activity activity = activityRepository.findById(activityId).orElse(null);

        if (activity == null) {
            return null;
        }

        Branch branch = branchRepository.findById(activity.getBranchId()).orElse(null);

        return activityCancelledTelegramBuilder.build(activity, branch, recipientNameKm);
    }

    /**
     * The organizer branch is looked up from the activity's own
     * {@code branchId} (the host that prepared the certificates), matching
     * {@link NotificationEmailSender#sendCertificateReady}.
     */
    private String buildCertificateReadyText(Long activityId, String recipientNameKm) {
        Activity activity = activityRepository.findById(activityId).orElse(null);

        if (activity == null) {
            return null;
        }

        Branch organizerBranch = activity.getBranchId() == null
                ? null
                : branchRepository.findById(activity.getBranchId()).orElse(null);

        return certificateReadyTelegramBuilder.build(activity, organizerBranch, recipientNameKm);
    }

    private String buildDocumentIssuedText(Long documentId, String recipientNameKm) {
        Document document = documentRepository.findById(documentId).orElse(null);

        if (document == null) {
            return null;
        }

        return documentIssuedTelegramBuilder.build(document, recipientNameKm);
    }

    private String buildActivityReminderText(Long activityId, String recipientNameKm) {
        Activity activity = activityRepository.findById(activityId).orElse(null);

        if (activity == null) {
            return null;
        }

        Branch branch = activity.getBranchId() == null
                ? null
                : branchRepository.findById(activity.getBranchId()).orElse(null);

        return activityReminderTelegramBuilder.build(activity, branch, recipientNameKm);
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
