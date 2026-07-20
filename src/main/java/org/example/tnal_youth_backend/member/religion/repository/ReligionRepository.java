package org.example.tnal_youth_backend.member.religion.repository;

import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReligionRepository
        extends JpaRepository<Religion, Short> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            Short id
    );

    Optional<Religion> findByCodeIgnoreCase(String code);

    List<Religion> findAllByOrderBySortOrderAscIdAsc();

    List<Religion> findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}