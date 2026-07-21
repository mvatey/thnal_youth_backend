package org.example.tnal_youth_backend.activity.repository;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ActivityRepository
        extends JpaRepository<Activity, Long>,
        JpaSpecificationExecutor<Activity> {

    List<Activity> findAllByBranchIdOrderByStartsAtDesc(
            Long branchId
    );

    List<Activity>
    findAllByPublicActivityTrueAndStartsAtAfterOrderByStartsAtAsc(
            OffsetDateTime now
    );

    long countByBranchId(Long branchId);

    boolean existsByIdAndBranchId(
            Long activityId,
            Long branchId
    );
}