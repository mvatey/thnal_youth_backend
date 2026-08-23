package org.example.tnal_youth_backend.notification.dispatch;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
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
 * <p>Activity-invitation notifications ("ACTIVITY_INVITATION") get the
 * richer bilingual HTML invitation card instead of the generic plain-text
 * email every other notification type still gets.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailSender {

    private static final String ACTIVITY_INVITATION_TYPE_CODE = "ACTIVITY_INVITATION";

    private final JavaMailSender mailSender;
    private final ActivityRepository activityRepository;
    private final BranchRepository branchRepository;
    private final ActivityInvitationEmailBuilder activityInvitationEmailBuilder;

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
