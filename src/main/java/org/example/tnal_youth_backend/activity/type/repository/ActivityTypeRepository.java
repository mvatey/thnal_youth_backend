package org.example.tnal_youth_backend.activity.type.repository;

import org.example.tnal_youth_backend.activity.type.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityTypeRepository
        extends JpaRepository<ActivityType, Short> {

    List<ActivityType>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}