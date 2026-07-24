package org.example.tnal_youth_backend.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Mirrors the `notifications` table (V11 + V21).
 * Columns: type_id, title, body, action_url, activity_id, branch_id,
 * created_by, client_request_id, created_at. There are NO per-channel flags in
 * the schema — an in-app notification IS a set of notification_recipients rows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationModel {
    private Long id;
    private Short typeId;
    private String title;
    private String body;
    private String actionUrl;
    private Long activityId;
    private Long branchId;
    private Long createdBy;
    /** Optional idempotency key (V21). Passed to SQL as text and cast to uuid. */
    private String clientRequestId;
    private OffsetDateTime createdAt;
}
