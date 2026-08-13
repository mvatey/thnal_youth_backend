package org.example.tnal_youth_backend.member.proficiency.repository;

import org.example.tnal_youth_backend.member.proficiency.entity.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProficiencyLevelRepository
        extends JpaRepository<ProficiencyLevel, Short> {

    boolean
    existsByCodeIgnoreCase(
            String code
    );

    List<ProficiencyLevel>
    findAllByOrderBySortOrderAscIdAsc();

    List<ProficiencyLevel>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}