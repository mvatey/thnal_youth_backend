package org.example.tnal_youth_backend.notification.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends a notification's title/body by email, reusing the same
 * {@link JavaMailSender} bean {@code EmailOtpDeliveryService} already uses
 * for OTP emails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailSender {

    private final JavaMailSender mailSender;

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
