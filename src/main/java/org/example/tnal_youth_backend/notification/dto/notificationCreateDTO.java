package org.example.tnal_youth_backend.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class notificationCreateDTO {

    @NotNull
    private Short typeId;

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    private String linkUrl;
    private Long programId;
    private Long branchId;

    private Boolean sentViaInApp = Boolean.TRUE;
    private Boolean sentViaSms   = Boolean.FALSE;
    private Boolean sentViaEmail = Boolean.FALSE;

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