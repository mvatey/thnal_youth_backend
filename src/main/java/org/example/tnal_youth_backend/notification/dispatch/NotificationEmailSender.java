package org.example.tnal_youth_backend.notification.dispatch;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
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
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Sends a notification's title/body by email, reusing the same
 * {@link JavaMailSender} bean {@code EmailOtpDeliveryService} already uses
 * for OTP emails.
 *
 * <p>Activity-invitation ("ACTIVITY_INVITATION") and activity-rescheduled
 * ("ACTIVITY_UPDATED") notifications get the richer bilingual HTML card
 * instead of the generic plain-text email every other notification type
 * still gets — matching the same two message types Telegram already sends
 * as a long bilingual letter (see {@link TelegramMessageSender}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailSender {

    private static final String ACTIVITY_INVITATION_TYPE_CODE = "ACTIVITY_INVITATION";
    private static final String ACTIVITY_UPDATED_TYPE_CODE = "ACTIVITY_UPDATED";
    private static final String CERTIFICATE_READY_TYPE_CODE = "ACTIVITY_CERTIFICATE_READY";
    private static final String DOCUMENT_ADDED_TYPE_CODE = "DOCUMENT_ADDED";
    private static final String ACTIVITY_REMINDER_TYPE_CODE = "ACTIVITY_REMINDER";

    private final JavaMailSender mailSender;
    private final ActivityRepository activityRepository;
    private final BranchRepository branchRepository;
    private final DocumentRepository documentRepository;
    private final ActivityInvitationEmailBuilder activityInvitationEmailBuilder;
    private final ActivityRescheduledEmailBuilder activityRescheduledEmailBuilder;
    private final CertificateReadyEmailBuilder certificateReadyEmailBuilder;
    private final DocumentIssuedEmailBuilder documentIssuedEmailBuilder;
    private final ActivityReminderEmailBuilder activityReminderEmailBuilder;

    @Value("${app.mail.from}")
    private String fromAddress;

    /**
     * Base URL used to turn a notification's relative {@code actionUrl}
     * (e.g. {@code /activity/42}) into an absolute link an email client can
     * follow. Left blank by default — the user fills in the real frontend
     * origin (e.g. {@code https://app.tnalyouth.org}) in
     * {@code application.properties}.
     */
    @Value("${app.notification.base-url:}")
    private String baseUrl;

    public void send(User user, NotificationModel notification) {
        if (ACTIVITY_INVITATION_TYPE_CODE.equals(notification.getTypeCode())
                && notification.getActivityId() != null
                && sendActivityInvitation(user, notification)) {
            return;
        }

        if (ACTIVITY_UPDATED_TYPE_CODE.equals(notification.getTypeCode())
                && notification.getActivityId() != null
                && sendActivityRescheduled(user, notification)) {
            return;
        }

        if (CERTIFICATE_READY_TYPE_CODE.equals(notification.getTypeCode())
                && notification.getActivityId() != null
                && sendCertificateReady(user, notification)) {
            return;
        }

        if (DOCUMENT_ADDED_TYPE_CODE.equals(notification.getTypeCode())
                && notification.getDocumentId() != null
                && sendDocumentIssued(user, notification)) {
            return;
        }

        if (ACTIVITY_REMINDER_TYPE_CODE.equals(notification.getTypeCode())
                && notification.getActivityId() != null
                && sendActivityReminder(user, notification)) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(notification.getTitle());
        message.setText(buildBody(notification));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("NotificationEmailSender: failed to send to {}", user.getEmail(), e);
            throw e;
        }
    }

    /**
     * @return true if the rich HTML invitation was sent — false only when
     * the activity can no longer be found, in which case the caller falls
     * back to the plain-text email instead of sending nothing.
     */
    private boolean sendActivityInvitation(User user, NotificationModel notification) {
        Activity activity = activityRepository
                .findById(notification.getActivityId())
                .orElse(null);

        if (activity == null) {
            return false;
        }

        Branch branch = branchRepository
                .findById(activity.getBranchId())
                .orElse(null);

        String html = activityInvitationEmailBuilder.build(activity, branch, user.getFullNameKm());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(activity.getTitleKm());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            log.warn("NotificationEmailSender: failed to send activity invitation to {}", user.getEmail(), e);
            throw new IllegalStateException(
                    "Failed to send activity invitation email: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * @return true if the rich HTML reschedule notice was sent — false only
     * when the activity can no longer be found, in which case the caller
     * falls back to the plain-text email instead of sending nothing.
     */
    private boolean sendActivityRescheduled(User user, NotificationModel notification) {
        Activity activity = activityRepository
                .findById(notification.getActivityId())
                .orElse(null);

        if (activity == null) {
            return false;
        }

        Branch branch = branchRepository
                .findById(activity.getBranchId())
                .orElse(null);

        String html = activityRescheduledEmailBuilder.build(activity, branch, user.getFullNameKm());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(activity.getTitleKm());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            log.warn("NotificationEmailSender: failed to send activity reschedule notice to {}", user.getEmail(), e);
            throw new IllegalStateException(
                    "Failed to send activity reschedule email: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * @return true if the rich HTML certificate-ready notice was sent —
     * false only when the activity can no longer be found, in which case
     * the caller falls back to the plain-text email instead of sending
     * nothing. The organizer branch is looked up from the activity's own
     * {@code branchId} (the host that prepared the certificates), not
     * {@code notification.getBranchId()} (the co-hosting recipient branch).
     */
    private boolean sendCertificateReady(User user, NotificationModel notification) {
        Activity activity = activityRepository
                .findById(notification.getActivityId())
                .orElse(null);

        if (activity == null) {
            return false;
        }

        Branch organizerBranch = activity.getBranchId() == null
                ? null
                : branchRepository.findById(activity.getBranchId()).orElse(null);

        String html = certificateReadyEmailBuilder.build(activity, organizerBranch, user.getFullNameKm());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(activity.getTitleKm());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            log.warn("NotificationEmailSender: failed to send certificate-ready notice to {}", user.getEmail(), e);
            throw new IllegalStateException(
                    "Failed to send certificate-ready email: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * @return true if the rich HTML document-issued notice was sent — false
     * only when the document can no longer be found, in which case the
     * caller falls back to the plain-text email instead of sending nothing.
     */
    private boolean sendDocumentIssued(User user, NotificationModel notification) {
        Document document = documentRepository
                .findById(notification.getDocumentId())
                .orElse(null);

        if (document == null) {
            return false;
        }

        String html = documentIssuedEmailBuilder.build(document, user.getFullNameKm());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(document.getTitle());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            log.warn("NotificationEmailSender: failed to send document-issued notice to {}", user.getEmail(), e);
            throw new IllegalStateException(
                    "Failed to send document-issued email: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * @return true if the rich HTML reminder was sent — false only when the
     * activity can no longer be found, in which case the caller falls back
     * to the plain-text email instead of sending nothing.
     */
    private boolean sendActivityReminder(User user, NotificationModel notification) {
        Activity activity = activityRepository
                .findById(notification.getActivityId())
                .orElse(null);

        if (activity == null) {
            return false;
        }

        Branch branch = activity.getBranchId() == null
                ? null
                : branchRepository.findById(activity.getBranchId()).orElse(null);

        String html = activityReminderEmailBuilder.build(activity, branch, user.getFullNameKm());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(activity.getTitleKm());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            log.warn("NotificationEmailSender: failed to send activity reminder to {}", user.getEmail(), e);
            throw new IllegalStateException(
                    "Failed to send activity reminder email: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e
            );
        }
    }

    private String buildBody(NotificationModel notification) {
        String body = notification.getBody() == null ? "" : notification.getBody();
        String link = resolveAbsoluteActionUrl(notification.getActionUrl());

        if (link == null) {
            return body;
        }

        return body + "\n\n" + link;
    }

    private String resolveAbsoluteActionUrl(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) {
            return null;
        }

        if (actionUrl.startsWith("http://") || actionUrl.startsWith("https://")) {
            return actionUrl;
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            // No base URL configured yet — a relative path isn't
            // clickable from an email client, so omit it rather than
            // send a broken link.
            return null;
        }

        String normalizedBase = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        String normalizedPath = actionUrl.startsWith("/") ? actionUrl : "/" + actionUrl;

        return normalizedBase + normalizedPath;
    }
}
