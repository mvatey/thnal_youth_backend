package org.example.tnal_youth_backend.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.tnal_youth_backend.notification.validation.SafeLink;

import java.util.List;

@Data
public class NotificationCreateDTO {

    @NotNull
    private Short typeId;

    @NotBlank
    @Size(max = 200, message = "title must be 200 characters or fewer")
    private String title;

    @NotBlank
    @Size(max = 4000, message = "body must be 4000 characters or fewer")
    private String body;

    /**
     * Optional deep link. Must be an app-relative path ("/...") or an http(s)
     * URL on an allowed host. {@link SafeLink} rejects protocol-relative URLs,
     * {@code javascript:}/{@code data:} schemes, and off-site hosts — closing the
     * open-redirect / XSS gap the old {@code @Pattern("^(https?://|/).*")} left open.
     * Persisted to notifications.action_url.
     */
    @SafeLink
    @Size(max = 2000, message = "actionUrl must be 2000 characters or fewer")
    private String actionUrl;

    /** Optional context: the activity this notification is about (notifications.activity_id). */
    private Long activityId;

    /** Optional context: the branch this notification is about (notifications.branch_id). */
    private Long branchId;

    /**
     * Optional idempotency key (client-generated UUID). Two creates from the same
     * admin with the same clientRequestId collapse to one notification — protecting
     * against double-submit / retries fanning out twice. Omit it to keep the
     * previous (non-idempotent) behaviour.
     */
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "clientRequestId must be a UUID")
    private String clientRequestId;

    /** Fan-out target. Required. */
    @NotNull
    private TargetMode target;

    /** Required if target = BRANCH. */
    private Long targetBranchId;

    /** Required if target = ACTIVITY_PARTICIPANTS. */
    private Long targetActivityId;

    /** Required if target = USERS. */
    private List<Long> targetUserIds;

    public enum TargetMode { ALL, BRANCH, ACTIVITY_PARTICIPANTS, USERS }
}
