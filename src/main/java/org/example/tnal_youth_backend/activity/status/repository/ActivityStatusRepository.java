package org.example.tnal_youth_backend.activity.status.repository;

import org.example.tnal_youth_backend.activity.status.entity.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityStatusRepository
        extends JpaRepository<ActivityStatus, Short> {

    List<ActivityStatus>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}