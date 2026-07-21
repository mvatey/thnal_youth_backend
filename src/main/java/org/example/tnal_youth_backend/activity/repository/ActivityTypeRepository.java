package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityTypeRepository
        extends JpaRepository<ActivityType, Short> {

    Optional<ActivityType> findByCodeIgnoreCase(String code);

    List<ActivityType>
    findAllByActiveTrueOrderBySortOrderAsc();
}