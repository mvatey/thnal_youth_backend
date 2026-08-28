package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository
        extends JpaRepository<ActivityParticipant, Long> {

    /*
     * Activity-side participant list.
     */
    @EntityGraph(
            attributePaths = {
                    "activity",
                    "activity.type",
                    "activity.sector",
                    "member",
                    "attendanceStatus",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    List<ActivityParticipant>
    findAllByActivity_IdOrderByRegisteredAtDesc(
            Long activityId
    );

    /*
     * Member participation table with:
     *
     * - pagination
     * - search
     * - activity type filter
     * - attendance status filter
     */
    @EntityGraph(
            attributePaths = {
                    "activity",
                    "activity.type",
                    "activity.sector",
                    "member",
                    "attendanceStatus",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    @Query("""
            SELECT participant
            FROM ActivityParticipant participant
            JOIN participant.activity activity

            WHERE participant.member.id = :memberId

              AND (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(
                            activity.titleKm
                       )
                       LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                       )
                    OR LOWER(
                            COALESCE(
                                activity.titleEn,
                                ''
                            )
                       )
                       LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                       )
                    OR LOWER(
                            COALESCE(
                                activity.locationName,
                                ''
                            )
                       )
                       LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                       )
              )

              AND (
                    :typeId IS NULL
                    OR activity.type.id = :typeId
              )

              AND (
                    :attendanceStatusId IS NULL
                    OR participant.attendanceStatus.id
                        = :attendanceStatusId
              )

            ORDER BY
                activity.startsAt DESC,
                participant.id DESC
            """)
    Page<ActivityParticipant>
    findMemberParticipationPage(
            @Param("memberId")
            Long memberId,

            @Param("search")
            String search,

            @Param("typeId")
            Short typeId,

            @Param("attendanceStatusId")
            Short attendanceStatusId,

            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "activity.type",
                    "activity.sector",
                    "member",
                    "attendanceStatus",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    Optional<ActivityParticipant>
    findByActivity_IdAndMember_Id(
            Long activityId,
            Long memberId
    );

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "activity.type",
                    "activity.sector",
                    "member",
                    "attendanceStatus",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    Optional<ActivityParticipant>
    findByIdAndMember_Id(
            Long participationId,
            Long memberId
    );

    boolean existsByActivity_IdAndMember_Id(
            Long activityId,
            Long memberId
    );

    boolean existsByActivity_IdAndMember_IdAndIdNot(
            Long activityId,
            Long memberId,
            Long participationId
    );

    long countByActivity_Id(
            Long activityId
    );

    @Query("""
            SELECT COUNT(
                DISTINCT participant.activity.id
            )
            FROM ActivityParticipant participant
            JOIN participant.attendanceStatus attendanceStatus

            WHERE participant.member.id = :memberId
              AND UPPER(
                    attendanceStatus.code
                  ) = 'PRESENT'
            """)
    long countParticipatedActivitiesByMemberId(
            @Param("memberId")
            Long memberId
    );

    @Query("""
            SELECT COUNT(
                DISTINCT participant.activity.id
            )
            FROM ActivityParticipant participant
            JOIN participant.attendanceStatus attendanceStatus

            WHERE participant.member.id = :memberId
              AND UPPER(
                    attendanceStatus.code
                  ) = 'ABSENT'
            """)
    long countAbsentActivitiesByMemberId(
            @Param("memberId")
            Long memberId
    );

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM activity_participants ap
                        JOIN attendance_statuses ats
                          ON ats.id = ap.attendance_status_id
                        WHERE ap.member_id = :memberId
                          AND ap.activity_id = :activityId
                          AND UPPER(
                                ats.code
                              ) = 'PRESENT'
                    )
                    """,
            nativeQuery = true
    )
    boolean existsPresentParticipation(
            @Param("memberId")
            Long memberId,

            @Param("activityId")
            Long activityId
    );

    /*
     * Used by the "1 day before" reminder scheduler to find participants of
     * a given activity who have not yet been reminded.
     */
    List<ActivityParticipant> findAllByActivity_IdAndReminderSentAtIsNull(
            Long activityId
    );

    /*
     * Used to scope a member's own activity list/detail access to only the
     * activities they were personally invited to.
     */
    @Query("""
            SELECT DISTINCT participant.activity.id
            FROM ActivityParticipant participant
            WHERE participant.member.id = :memberId
            """)
    List<Long> findDistinctActivityIdsByMemberId(
            @Param("memberId") Long memberId
    );

    /*
     * One row per activity containing the activity attendance ratio shown
     * in the list and detail: joined / attendance members. The denominator
     * includes invited members and any extra member staff later marks as
     * joined (a walk-in), so the ratio matches the attendance cards.
     */
    @Query(
            value = """
                    SELECT ap.activity_id AS activityId,
                           SUM(CASE
                                 WHEN UPPER(COALESCE(ast.code, '')) = 'PRESENT'
                                      OR ap.checked_in_at IS NOT NULL
                                 THEN 1 ELSE 0
                               END) AS joinedCount,
                           COUNT(ap.id) AS invitedCount
                    FROM activity_participants ap
                    LEFT JOIN attendance_statuses ast
                           ON ast.id = ap.attendance_status_id
                    WHERE ap.activity_id IN (:activityIds)
                    GROUP BY ap.activity_id
                    """,
            nativeQuery = true
    )
    List<ActivityAttendanceCountProjection> countAttendanceGroupedByActivityIds(
            @Param("activityIds") Collection<Long> activityIds
    );

    interface ActivityAttendanceCountProjection {
        Long getActivityId();
        Long getJoinedCount();
        Long getInvitedCount();
    }

    /*
     * One row per activity id, counting EVERY participant regardless of
     * source (host branch, an accepted co-hosting branch, or a walk-in) —
     * same counting rule as validateCapacity. Used to populate
     * ActivityListItemResponse.participantCount for a page of activities in
     * a single query instead of one count query per row.
     */
    @Query("""
            SELECT participant.activity.id AS activityId,
                   COUNT(participant) AS participantCount
            FROM ActivityParticipant participant
            WHERE participant.activity.id IN :activityIds
            GROUP BY participant.activity.id
            """)
    List<ActivityParticipantCountProjection> countGroupedByActivityIds(
            @Param("activityIds") Collection<Long> activityIds
    );

    interface ActivityParticipantCountProjection {
        Long getActivityId();

        Long getParticipantCount();
    }
}
