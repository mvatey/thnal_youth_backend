package org.example.tnal_youth_backend.member.skill.repository;

import org.example.tnal_youth_backend.member.skill.entity.MemberSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberSkillRepository
        extends JpaRepository<MemberSkill, Long> {

    List<MemberSkill>
    findAllByMember_IdOrderByIdAsc(
            Long memberId
    );

    Optional<MemberSkill>
    findByIdAndMember_Id(
            Long skillId,
            Long memberId
    );

    boolean
    existsByMember_IdAndSkillNameIgnoreCase(
            Long memberId,
            String skillName
    );

    boolean
    existsByMember_IdAndSkillNameIgnoreCaseAndIdNot(
            Long memberId,
            String skillName,
            Long skillId
    );
}