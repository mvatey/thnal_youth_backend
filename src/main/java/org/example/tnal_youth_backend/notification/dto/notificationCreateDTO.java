package org.example.tnal_youth_backend.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class notificationCreateDTO {

    @NotNull
    private Short typeId;

    @NotBlank
    @Size(max = 200, message = "title must be 200 characters or fewer")
    private String title;

    @NotBlank
    @Size(max = 4000, message = "body must be 4000 characters or fewer")
    private String body;

    /**
     * Optional link. Must be http(s) or an app-relative path.
     * Prevents `javascript:` and other unsafe schemes reaching the notification tray.
     */
    @Pattern(regexp = "^(https?://|/).*",
            message = "linkUrl must be an http(s) URL or start with /")
    private String linkUrl;

    private Long programId;
    private Long branchId;

    // Defaults handled in service. Field-level defaults are misleading because
    // Jackson overwrites them with null on explicit JSON null.
    private Boolean sentViaInApp;
    private Boolean sentViaSms;
    private Boolean sentViaEmail;

    /** Fan-out target. Required. */
    @NotNull
    private TargetMode target;

    /** Required if target = BRANCH. */
    private Long targetBranchId;

    /** Required if target = PROGRAM_PARTICIPANTS. */
    private Long targetProgramId;

    /** Required if target = USERS. */
    private List<Long> targetUserIds;

    public enum TargetMode { ALL, BRANCH, PROGRAM_PARTICIPANTS, USERS }
}