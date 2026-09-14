package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityInvitedBranchRepository
        extends JpaRepository<ActivityInvitedBranch, Long> {

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "branch",
                    "invitedBy",
                    "respondedBy"
            }
    )
    List<ActivityInvitedBranch>
    findAllByActivity_IdOrderByInvitedAtDesc(
            Long activityId
    );

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "branch",
                    "invitedBy",
                    "respondedBy"
            }
    )
    Optional<ActivityInvitedBranch>
    findByIdAndActivity_Id(
            Long invitationId,
            Long activityId
    );

    Optional<ActivityInvitedBranch>
    findByActivity_IdAndBranch_Id(
            Long activityId,
            Long branchId
    );

    /*
     * Every invitation still awaiting a response for one activity — used to
     * auto-decline them once the activity concludes (see
     * ActivityServiceImpl#declineStalePendingInvitations), since accepting
     * an invitation to a completed/cancelled activity makes no sense.
     */
    List<ActivityInvitedBranch>
    findAllByActivity_IdAndInvitationStatus(
            Long activityId,
            ActivityInvitationStatus invitationStatus
    );

    Optional<ActivityInvitedBranch>
    findByActivity_IdAndBranch_IdAndInvitationStatus(
            Long activityId,
            Long branchId,
            ActivityInvitationStatus invitationStatus
    );

    boolean existsByActivity_IdAndBranch_IdAndInvitationStatusNot(
            Long activityId,
            Long branchId,
            ActivityInvitationStatus invitationStatus
    );

    /*
     * The ids of every activity that one of `branchIds` has an invitation
     * of the given status for — used to fold "invited to co-host" activities
     * into a branch-scoped staff member's activity list (see
     * ActivityServiceImpl.getActivities), alongside their own host-branch
     * activities.
     */
    @Query(
            "SELECT ib.activity.id FROM ActivityInvitedBranch ib "
                    + "WHERE ib.branch.id IN :branchIds "
                    + "AND ib.invitationStatus = :status"
    )
    List<Long> findActivityIdsByBranchIdInAndInvitationStatus(
            @Param("branchIds") Collection<Long> branchIds,
            @Param("status") ActivityInvitationStatus status
    );

    /*
     * Full invitation rows (not just activity ids) for one of `branchIds`,
     * restricted to the given statuses — used to fold BOTH pending and
     * accepted co-hosting invitations into a branch-scoped staff member's
     * activity list (see ActivityServiceImpl.getActivities), so a PENDING
     * invitation can show up with an Accept/Decline action right there
     * instead of only being reachable via the notification link. DECLINED
     * and CANCELLED are deliberately excluded by the caller passing only
     * the statuses it wants — this method itself is status-agnostic.
     */
    @EntityGraph(
            attributePaths = {
                    "branch"
            }
    )
    @Query(
            "SELECT ib FROM ActivityInvitedBranch ib "
                    + "WHERE ib.branch.id IN :branchIds "
                    + "AND ib.invitationStatus IN :statuses"
    )
    List<ActivityInvitedBranch> findByBranchIdInAndInvitationStatusIn(
            @Param("branchIds") Collection<Long> branchIds,
            @Param("statuses") Collection<ActivityInvitationStatus> statuses
    );

    /*
     * Every invitation still PENDING whose activity has already ended --
     * used by ActivityStatusScheduler to auto-decline stale invitations on
     * a timer. ActivityServiceImpl#completeActivity already declines these
     * too, but only when staff manually mark the activity COMPLETED, which
     * the frontend's own effective-status display (endsAt in the past) does
     * not require them to ever actually do -- so a PENDING invitation could
     * otherwise sit there indefinitely, still showing Accept/Decline, for
     * an activity that already happened.
     */
    @EntityGraph(
            attributePaths = {
                    "activity"
            }
    )
    List<ActivityInvitedBranch>
    findAllByInvitationStatusAndActivity_EndsAtLessThanEqual(
            ActivityInvitationStatus invitationStatus,
            OffsetDateTime cutoff
    );
}
