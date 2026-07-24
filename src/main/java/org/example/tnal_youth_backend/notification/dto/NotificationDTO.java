package org.example.tnal_youth_backend.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class NotificationDTO {
    private Long id;
    private String typeCode;
    private String typeLabelKm;
    private String typeLabelEn;
    private String title;
    private String body;
    private String actionUrl;
    private Long activityId;
    private Long branchId;
    private OffsetDateTime createdAt;

    @JsonProperty("isRead")
    private boolean isRead;
    private OffsetDateTime readAt; // null => unread (guaranteed by the read-state CHECK)
}
