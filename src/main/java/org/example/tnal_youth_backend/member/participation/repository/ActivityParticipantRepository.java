package org.example.tnal_youth_backend.member.participation.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("memberActivityParticipantRepository")
public interface ActivityParticipantRepository
        extends JpaRepository<ActivityParticipant, Long> {

    List<ActivityParticipant>
    findAllByMemberIdOrderByRegisteredAtDescIdDesc(
            Long memberId
    );

    Optional<ActivityParticipant>
    findByIdAndMemberId(
            Long id,
            Long memberId
    );

    boolean existsByActivityIdAndMemberId(
            Long activityId,
            Long memberId
    );

    boolean existsByActivityIdAndMemberIdAndIdNot(
            Long activityId,
            Long memberId,
            Long id
    );
}