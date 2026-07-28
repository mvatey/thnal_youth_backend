package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * Member participation table with pagination,
     * search, and activity-type filtering.
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
                    OR LOWER(activity.titleKm)
                        LIKE LOWER(
                            CONCAT('%', :search, '%')
                        )
                    OR LOWER(
                            COALESCE(
                                activity.titleEn,
                                ''
                            )
                        )
                        LIKE LOWER(
                            CONCAT('%', :search, '%')
                        )
                    OR LOWER(
                            COALESCE(
                                activity.locationName,
                                ''
                            )
                        )
                        LIKE LOWER(
                            CONCAT('%', :search, '%')
                        )
              )

              AND (
                    :typeId IS NULL
                    OR activity.type.id = :typeId
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
}