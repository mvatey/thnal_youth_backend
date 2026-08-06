package org.example.tnal_youth_backend.member.education.repository;

import org.example.tnal_youth_backend.member.education.entity.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationLevelRepository
        extends JpaRepository<EducationLevel, Short> {

    List<EducationLevel>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}