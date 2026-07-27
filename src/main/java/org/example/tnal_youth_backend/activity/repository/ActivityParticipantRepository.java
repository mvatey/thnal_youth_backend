package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository
        extends JpaRepository<ActivityParticipant, Long> {

    // Activity-side participant list
    @EntityGraph(
            attributePaths = {
                    "activity",
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

    // Member-side participation history
    @EntityGraph(
            attributePaths = {
                    "activity",
                    "member",
                    "attendanceStatus",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    List<ActivityParticipant>
    findAllByMember_IdOrderByRegisteredAtDescIdDesc(
            Long memberId
    );

    @EntityGraph(
            attributePaths = {
                    "activity",
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