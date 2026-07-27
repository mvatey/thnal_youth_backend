package org.example.tnal_youth_backend.activity.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityInvitedBranchResponse {

    private Long id;

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("branch_name_km")
    private String branchNameKm;

    @JsonProperty("branch_name_en")
    private String branchNameEn;

    @JsonProperty("invitation_status")
    private ActivityInvitationStatus invitationStatus;

    @JsonProperty("can_manage_attendance")
    private Boolean canManageAttendance;

    @JsonProperty("can_record_donation")
    private Boolean canRecordDonation;

    @JsonProperty("invited_by")
    private Long invitedBy;

    @JsonProperty("invited_at")
    private OffsetDateTime invitedAt;

    @JsonProperty("responded_by")
    private Long respondedBy;

    @JsonProperty("responded_at")
    private OffsetDateTime respondedAt;

    private String note;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}