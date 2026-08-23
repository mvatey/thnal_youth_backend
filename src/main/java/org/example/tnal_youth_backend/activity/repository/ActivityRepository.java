package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    Page<Activity> findAllByBranchIdIn(
            Collection<Long> branchIds,
            Pageable pageable
    );

    /*
     * Same as findAllByBranchIdIn, but also folds in a second, independent
     * set of activity ids — used to add "invited to co-host" activities
     * (whose host branch is someone else's) alongside a staff member's own
     * host-branch activities. Only called when that second set is
     * non-empty; an empty IN(...) is avoided by the caller.
     */
    Page<Activity> findAllByBranchIdInOrIdIn(
            Collection<Long> branchIds,
            Collection<Long> ids,
            Pageable pageable
    );

    List<Activity> findAllByStatus_CodeIgnoreCaseAndStartsAtLessThanEqual(
            String statusCode,
            OffsetDateTime currentTime
    );

    long countByBranchId(Long branchId);

    @Query(
            value = """
        SELECT COUNT(DISTINCT activity.id)
        FROM activities activity
        INNER JOIN members member
                ON member.id = :memberId
        WHERE activity.ends_at < :now
          AND (
                activity.branch_id = member.branch_id
                OR EXISTS (
                    SELECT 1
                    FROM activity_invited_branches invited_branch
                    WHERE invited_branch.activity_id = activity.id
                      AND invited_branch.branch_id = member.branch_id
                      AND invited_branch.invitation_status = 'ACCEPTED'
                )
          )
          AND NOT EXISTS (
                SELECT 1
                FROM activity_participants participant
                WHERE participant.activity_id = activity.id
                  AND participant.member_id = member.id
          )
        """,
            nativeQuery = true
    )
    long countCompletedRelevantActivitiesNotJoined(
            @Param("memberId") Long memberId,
            @Param("now") OffsetDateTime now
    );

    /*
     * Used by the "1 day before" reminder scheduler to find every activity
     * starting within a given window (typically "tomorrow", midnight to
     * midnight).
     */
    List<Activity> findAllByStartsAtGreaterThanEqualAndStartsAtLessThan(
            OffsetDateTime from,
            OffsetDateTime to
    );

    /*
     * Used to page through a specific, already-known set of activity ids —
     * e.g. the activities a member was personally invited to.
     */
    Page<Activity> findAllByIdIn(
            Collection<Long> ids,
            Pageable pageable
    );
}
