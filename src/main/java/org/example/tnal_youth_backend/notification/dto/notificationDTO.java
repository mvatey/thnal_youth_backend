package org.example.tnal_youth_backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Row returned to the current user for their inbox. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class notificationDTO {
    private Long id;
    private String typeCode;
    private String typeLabelKm;
    private String typeLabelEn;
    private String title;
    private String body;
    private String linkUrl;
    private Long programId;
    private Long branchId;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt; // null => unread
}