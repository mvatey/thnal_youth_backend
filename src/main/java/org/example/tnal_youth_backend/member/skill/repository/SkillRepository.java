package org.example.tnal_youth_backend.member.skill.repository;

import org.example.tnal_youth_backend.member.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository
        extends JpaRepository<Skill, Short> {

    boolean
    existsByCodeIgnoreCase(
            String code
    );

    List<Skill>
    findAllByOrderBySortOrderAscIdAsc();

    List<Skill>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}