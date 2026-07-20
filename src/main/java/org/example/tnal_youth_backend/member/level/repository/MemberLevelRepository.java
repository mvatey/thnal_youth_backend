package org.example.tnal_youth_backend.member.level.repository;

import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLevelRepository
        extends JpaRepository<MemberLevel, Short> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            Short id
    );

    Optional<MemberLevel> findByCodeIgnoreCase(String code);

    List<MemberLevel> findAllByOrderBySortOrderAscIdAsc();

    List<MemberLevel> findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}