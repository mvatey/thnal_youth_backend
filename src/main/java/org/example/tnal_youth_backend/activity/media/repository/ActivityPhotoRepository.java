package org.example.tnal_youth_backend.activity.media.repository;

import org.example.tnal_youth_backend.activity.media.entity.ActivityPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityPhotoRepository
        extends JpaRepository<ActivityPhoto, Long> {

    /**
     * Find all gallery images of an activity.
     */
    List<ActivityPhoto> findByActivityIdOrderBySortOrderAscIdAsc(
            Long activityId
    );

    /**
     * Find one gallery image belonging to an activity.
     */
    Optional<ActivityPhoto> findByIdAndActivityId(
            Long photoId,
            Long activityId
    );

    /**
     * Delete one gallery image belonging to an activity.
     */
    void deleteByIdAndActivityId(
            Long photoId,
            Long activityId
    );

    /**
     * Count gallery images.
     */
    long countByActivityId(
            Long activityId
    );

    /**
     * Find the highest sort order.
     */
    Optional<ActivityPhoto> findTopByActivityIdOrderBySortOrderDesc(
            Long activityId
    );

    /**
     * Check whether a file is already linked to the activity.
     */
    boolean existsByActivityIdAndFileId(
            Long activityId,
            Long fileId
    );
}