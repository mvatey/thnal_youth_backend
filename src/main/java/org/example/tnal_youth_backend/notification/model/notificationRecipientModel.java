package org.example.tnal_youth_backend.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class notificationRecipientModel {
    private Long id;
    private Long notificationId;
    private Long userId;
    private OffsetDateTime readAt;
}