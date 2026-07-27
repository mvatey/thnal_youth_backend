package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ActivityRepository
        extends JpaRepository<Activity, Long> {

    List<Activity> findAllByStatus_CodeIgnoreCaseAndStartsAtLessThanEqual(
            String statusCode,
            OffsetDateTime currentTime
    );
}