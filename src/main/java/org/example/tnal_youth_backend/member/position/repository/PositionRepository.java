package org.example.tnal_youth_backend.member.position.repository;

import org.example.tnal_youth_backend.member.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository
        extends JpaRepository<Position, Short> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code,
            Short id
    );

    Optional<Position> findByCodeIgnoreCase(String code);

    List<Position> findAllByOrderBySortOrderAscIdAsc();

    List<Position> findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}
