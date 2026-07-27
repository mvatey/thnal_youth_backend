package org.example.tnal_youth_backend.member.workhistory.repository;

import org.example.tnal_youth_backend.member.workhistory.entity.MemberWorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberWorkHistoryRepository
        extends JpaRepository<MemberWorkHistory, Long> {

    List<MemberWorkHistory>
    findAllByMemberIdOrderByStartDateDescIdDesc(
            Long memberId
    );

    Optional<MemberWorkHistory>
    findByIdAndMemberId(
            Long id,
            Long memberId
    );
}