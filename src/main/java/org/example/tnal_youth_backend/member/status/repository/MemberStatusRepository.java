package org.example.tnal_youth_backend.member.status.repository;

import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberStatusRepository
        extends JpaRepository<MemberStatus, Short> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            Short id
    );

    Optional<MemberStatus> findByCodeIgnoreCase(String code);

    List<MemberStatus> findAllByOrderBySortOrderAscIdAsc();

    List<MemberStatus> findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}