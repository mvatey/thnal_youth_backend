package org.example.tnal_youth_backend.notification.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.example.tnal_youth_backend.notification.repo.NotificationRepo;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fans an already-committed in-app notification out to email/Telegram.
 *
 * <p>Runs {@code AFTER_COMMIT} (see {@link NotificationCreatedEvent}), so by
 * the time this executes the notification and its
 * {@code notification_recipients} rows are guaranteed to be durably
 * persisted — this is a separate, best-effort pass over already-real data,
 * not part of the original transaction. Each recipient's email/Telegram send
 * is individually wrapped in try/catch: one member's bounced email or
 * unlinked Telegram must never stop the rest of the fan-out from going out.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchListener {

    private final NotificationRepo notificationRepo;
    private final UserRepository userRepository;
    private final NotificationEmailSender emailSender;
    private final TelegramMessageSender telegramMessageSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        Long notificationId = event.notificationId();

        NotificationModel notification = notificationRepo.findById(notificationId);
        if (notification == null) {
            log.warn("NotificationDispatchListener: notification {} not found, skipping channel dispatch", notificationId);
            return;
        }

        List<Long> recipientUserIds = notificationRepo.findRecipientUserIds(notificationId);
        if (recipientUserIds.isEmpty()) {
            return;
        }

        for (Long userId : recipientUserIds) {
            dispatchToUser(notification, userId);
        }
    }

    private void dispatchToUser(NotificationModel notification, Long userId) {
        User user;
        try {
            user = userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            log.warn("NotificationDispatchListener: failed to load user {} for notification {}",
                    userId, notification.getId(), e);
            return;
        }

        if (user == null) {
            return;
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            try {
                emailSender.send(user, notification);
                recordDelivery(notification.getId(), userId, "EMAIL", "SENT", null);
            } catch (Exception e) {
                log.warn("NotificationDispatchListener: email send failed for user {} notification {}",
                        userId, notification.getId(), e);
                recordDelivery(notification.getId(), userId, "EMAIL", "FAILED", truncate(e.getMessage()));
            }
        } else {
            recordDelivery(notification.getId(), userId, "EMAIL", "SKIPPED", "no email on file");
        }

        if (user.getTelegramChatId() != null) {
            try {
                telegramMessageSender.send(user, notification);
                recordDelivery(notification.getId(), userId, "TELEGRAM", "SENT", null);
            } catch (Exception e) {
                log.warn("NotificationDispatchListener: telegram send failed for user {} notification {}",
                        userId, notification.getId(), e);
                recordDelivery(notification.getId(), userId, "TELEGRAM", "FAILED", truncate(e.getMessage()));
            }
        } else {
            recordDelivery(notification.getId(), userId, "TELEGRAM", "SKIPPED", "not linked");
        }
    }

    /**
     * Best-effort audit row — a failure here (e.g. a transient DB hiccup)
     * is only logged, never allowed to mask the actual send outcome above.
     */
    private void recordDelivery(Long notificationId, Long userId, String channel, String status, String errorMessage) {
        try {
            notificationRepo.insertDelivery(notificationId, userId, channel, status, errorMessage);
        } catch (Exception e) {
            log.warn("NotificationDispatchListener: failed to record {} delivery for user {} notification {}",
                    channel, userId, notificationId, e);
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
