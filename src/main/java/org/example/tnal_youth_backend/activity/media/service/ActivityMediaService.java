package org.example.tnal_youth_backend.activity.media.service;

import org.example.tnal_youth_backend.activity.media.dto.response.ActivityCoverImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ActivityMediaService {

    ActivityCoverImageResponse uploadCoverImage(
            Long activityId,
            MultipartFile file,
            Long currentUserId
    );

    ActivityCoverImageResponse getCoverImage(
            Long activityId
    );

    void deleteCoverImage(
            Long activityId,
            Long currentUserId
    );
}