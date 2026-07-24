package org.example.tnal_youth_backend.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Mirrors the `notification_recipients` table (V11).
 *
 * The read state is governed by chk_notification_recipient_read_state:
 *   (is_read = FALSE AND read_at IS NULL) OR (is_read = TRUE AND read_at IS NOT NULL)
 * so the two fields must always be mutated together.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientModel {
    private Long id;
    private Long notificationId;
    private Long userId;
    private Boolean isRead;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;
}
