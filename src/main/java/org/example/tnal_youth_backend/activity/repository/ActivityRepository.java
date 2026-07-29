package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    List<Activity> findAllByStatus_CodeIgnoreCaseAndStartsAtLessThanEqual(
            String statusCode,
            OffsetDateTime currentTime
    );

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
}