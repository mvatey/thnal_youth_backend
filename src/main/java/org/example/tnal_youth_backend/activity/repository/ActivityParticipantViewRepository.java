package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface ActivityParticipantViewRepository
        extends Repository<
        ActivityParticipant,
        Long
        > {

    /*
     * ADMIN / VIEWER read-only view.
     */
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

    /*
     * Branch-scoped participant view.
     *
     * Host:
     * activity + host branch only
     *
     * Invited branch:
     * activity + that invited branch only
     */
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
    findAllByActivity_IdAndMember_BranchIdOrderByRegisteredAtDesc(
            Long activityId,
            Long branchId
    );

    /*
     * Used by the new:
     *
     * សមាជិកសាខាអញ្ជើញ
     *
     * card.
     *
     * All formally invited or walk-in members
     * from ACCEPTED invited branches use
     * registration source INVITED_BRANCH.
     */
    long countByActivity_IdAndRegistrationSource(
            Long activityId,
            ParticipantRegistrationSource
                    registrationSource
    );
}