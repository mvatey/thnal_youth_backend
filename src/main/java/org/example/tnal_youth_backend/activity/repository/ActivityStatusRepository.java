package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityStatusRepository
        extends JpaRepository<ActivityStatus, Short> {

    Optional<ActivityStatus> findByCodeIgnoreCase(String code);

    List<ActivityStatus>
    findAllByActiveTrueOrderBySortOrderAsc();
}