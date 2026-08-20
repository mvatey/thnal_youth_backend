package org.example.tnal_youth_backend.activity.model.response;

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
public class ActivityListItemResponse {

    private Long id;

    private String titleKm;

    private String titleEn;

    private ActivityResponse.LookupResponse type;

    private ActivityResponse.LookupResponse sector;

    private ActivityResponse.LookupResponse status;

    private Long branchId;

    private String branchNameKm;

    private String branchNameEn;

    private Boolean publicActivity;

    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    private String locationName;

    private Integer capacity;

    /*
     * Total number of ActivityParticipant rows for this activity, from
     * every source — the host branch's own members, an accepted co-hosting
     * branch's members, and walk-ins alike. Deliberately not branch-scoped:
     * capacity is a single shared cap on the whole activity (see
     * ActivityParticipantServiceImpl.validateCapacity, which counts the
     * same way), so a co-hosting branch's invited members must count
     * against it too, not just the host branch's own.
     */
    private Long participantCount;

    // Attendance summary displayed as member_joined / invited.
    private Long joinedCount;

    private Long invitedCount;

    /*
     * true  -> the viewer's own branch (or one of their staff-assigned
     *          branches) hosts this activity.
     * false -> the viewer's branch reaches this activity only through a
     *          PENDING or ACCEPTED co-hosting invitation (see
     *          ActivityInvitedBranch and invitationStatus below).
     * null  -> not computed for this viewer's role (e.g. ADMIN, who sees
     *          every activity unscoped and has no "own branch" to compare
     *          against).
     *
     * Set by ActivityServiceImpl.getActivities() after mapping, since the
     * mapper itself has no access to the current viewer's branch scope.
     */
    private Boolean ownBranch;

    /*
     * Only set when ownBranch == false (an invited activity) — the
     * underlying ActivityInvitedBranch row's id and status for whichever of
     * the viewer's own branches was invited. PENDING means the viewer's
     * branch hasn't responded yet (the frontend list shows Accept/Decline
     * for this row); ACCEPTED means it's an active co-hosting relationship
     * (the frontend shows the normal Detail action instead, which leads to
     * inviting the branch's own members). Never DECLINED/CANCELLED — those
     * statuses are excluded from the list entirely rather than shown inert.
     * Always null for an own-hosted row (ownBranch == true) or when
     * ownBranch itself is null.
     */
    private Long invitationId;

    /*
     * Branch represented by invitationId. This is intentionally separate
     * from branchId, which is always the activity's organizer. It lets the
     * frontend keep an invited activity visible when the global branch
     * selector is set to the invited/co-hosting branch.
     */
    private Long invitedBranchId;

    private ActivityInvitationStatus invitationStatus;
}
