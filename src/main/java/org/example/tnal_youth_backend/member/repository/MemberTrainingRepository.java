package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberTraining;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTrainingRepository extends JpaRepository<MemberTraining, Long> {
    List<MemberTraining> findByMemberId(Long memberId);
}
