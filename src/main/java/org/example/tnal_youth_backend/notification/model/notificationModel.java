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
public class notificationModel {
    private Long id;
    private Short typeId;
    private String title;
    private String body;
    private String linkUrl;
    private Long programId;
    private Long branchId;
    private Boolean sentViaInApp;
    private Boolean sentViaSms;
    private Boolean sentViaEmail;
    private Long createdBy;
    private OffsetDateTime createdAt;
}