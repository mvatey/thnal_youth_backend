package org.example.tnal_youth_backend.member.repository;

import org.example.tnal_youth_backend.member.entity.MemberSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberSkillRepository extends JpaRepository<MemberSkill, Long> {
    List<MemberSkill> findByMemberId(Long memberId);
}
