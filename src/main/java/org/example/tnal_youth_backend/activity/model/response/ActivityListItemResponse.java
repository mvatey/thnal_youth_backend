package org.example.tnal_youth_backend.activity.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    /*
     * true  -> the viewer's own branch (or one of their staff-assigned
     *          branches) hosts this activity.
     * false -> the viewer's branch reaches this activity only through an
     *          ACCEPTED co-hosting invitation (see ActivityInvitedBranch).
     * null  -> not computed for this viewer's role (e.g. ADMIN, who sees
     *          every activity unscoped and has no "own branch" to compare
     *          against).
     *
     * Set by ActivityServiceImpl.getActivities() after mapping, since the
     * mapper itself has no access to the current viewer's branch scope.
     */
    private Boolean ownBranch;
}