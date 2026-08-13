package org.example.tnal_youth_backend.member.education.repository;

import org.example.tnal_youth_backend.member.education.entity.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationLevelRepository
        extends JpaRepository<EducationLevel, Short> {

    boolean
    existsByCodeIgnoreCase(
            String code
    );

    List<EducationLevel>
    findAllByOrderBySortOrderAscIdAsc();

    List<EducationLevel>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}