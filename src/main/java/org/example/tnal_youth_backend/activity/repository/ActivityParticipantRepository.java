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

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "member",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    List<ActivityParticipant>
    findAllByActivity_IdOrderByRegisteredAtDesc(
            Long activityId
    );

    @EntityGraph(
            attributePaths = {
                    "activity",
                    "member",
                    "invitedBy",
                    "invitedBranch"
            }
    )
    Optional<ActivityParticipant>
    findByActivity_IdAndMember_Id(
            Long activityId,
            Long memberId
    );

    boolean existsByActivity_IdAndMember_Id(
            Long activityId,
            Long memberId
    );

    long countByActivity_Id(Long activityId);
}