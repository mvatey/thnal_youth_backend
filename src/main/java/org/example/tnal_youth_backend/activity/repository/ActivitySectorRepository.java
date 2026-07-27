package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivitySectorRepository
        extends JpaRepository<ActivitySector, Short> {

    Optional<ActivitySector> findByCodeIgnoreCase(String code);

    List<ActivitySector>
    findAllByActiveTrueOrderBySortOrderAsc();
}