package org.example.tnal_youth_backend.activity.media.repository;

import org.example.tnal_youth_backend.activity.media.entity.ActivityAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityAttachmentRepository
        extends JpaRepository<ActivityAttachment, Long> {

    List<ActivityAttachment> findByActivityIdOrderBySortOrderAscIdAsc(
            Long activityId
    );

    Optional<ActivityAttachment> findByIdAndActivityId(
            Long attachmentId,
            Long activityId
    );

    Optional<ActivityAttachment> findTopByActivityIdOrderBySortOrderDescIdDesc(
            Long activityId
    );

    boolean existsByActivityIdAndFileId(
            Long activityId,
            Long fileId
    );

    long countByActivityId(Long activityId);
}
