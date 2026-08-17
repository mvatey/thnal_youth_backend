package org.example.tnal_youth_backend.activity.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.model.enums.ActivityBranchRole;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;

import java.time.OffsetDateTime;

/**
 * One row of {@code GET /api/activities/{activityId}/branches} — every
 * branch connected to this activity, tagged with its role. Exactly one row
 * per activity has {@code role == ORGANIZER} (synthesized from
 * {@code activities.branch_id}); every other row has
 * {@code role == INVITED} and comes from an {@code activity_invited_branches}
 * row. See {@link ActivityBranchRole} for why this is computed at read time
 * rather than stored.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityBranchResponse {

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

    private ActivityBranchRole role;

    /**
     * Always {@code null} for the ORGANIZER row — acceptance is not a
     * concept that applies to the branch that created the activity.
     * Otherwise the underlying invitation's status (PENDING / ACCEPTED /
     * DECLINED / CANCELLED).
     */
    @JsonProperty("invitation_status")
    private ActivityInvitationStatus invitationStatus;

    /**
     * Always {@code true} for the ORGANIZER row (the organizer can always
     * manage its own activity). Otherwise the invited branch's own
     * {@code can_manage_attendance} flag.
     */
    @JsonProperty("can_manage_attendance")
    private Boolean canManageAttendance;

    /**
     * Always {@code true} for the ORGANIZER row. Otherwise the invited
     * branch's own {@code can_record_donation} flag.
     */
    @JsonProperty("can_record_donation")
    private Boolean canRecordDonation;

    /**
     * The {@code activity_invited_branches.id} this row came from —
     * {@code null} for the ORGANIZER row, since it has no invitation
     * record.
     */
    @JsonProperty("invitation_id")
    private Long invitationId;

    @JsonProperty("invited_at")
    private OffsetDateTime invitedAt;

    @JsonProperty("responded_at")
    private OffsetDateTime respondedAt;
}
