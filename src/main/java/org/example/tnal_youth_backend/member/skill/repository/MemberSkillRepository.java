package org.example.tnal_youth_backend.member.skill.repository;

import org.example.tnal_youth_backend.member.skill.entity.MemberSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberSkillRepository
        extends JpaRepository<MemberSkill, Long> {

    List<MemberSkill> findAllByMemberIdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberSkill> findByIdAndMemberId(
            Long id,
            Long memberId
    );

    boolean existsByMemberIdAndSkillNameIgnoreCase(
            Long memberId,
            String skillName
    );

    boolean existsByMemberIdAndSkillNameIgnoreCaseAndIdNot(
            Long memberId,
            String skillName,
            Long id
    );
}