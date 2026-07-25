package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}