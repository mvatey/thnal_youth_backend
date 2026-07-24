package org.example.tnal_youth_backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateResultDTO {
    private Long notificationId;
    private int recipientCount;
    private OffsetDateTime createdAt;
}