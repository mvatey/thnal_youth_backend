package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberWorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberWorkHistoryRepository extends JpaRepository<MemberWorkHistory, Long> {
    List<MemberWorkHistory> findByMemberId(Long memberId);
}
