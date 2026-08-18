package org.example.tnal_youth_backend.notification.dispatch;

/**
 * Published after a notification and its recipient fan-out have committed
 * (see {@code NotificationService#create} and
 * {@code ActivityReminderScheduler#sendReminderForActivity}), never before —
 * consumed by {@link NotificationDispatchListener} at
 * {@code AFTER_COMMIT} so a rolled-back transaction never results in an
 * email/Telegram message for a notification that doesn't actually exist.
 */
public record NotificationCreatedEvent(Long notificationId) {
}
