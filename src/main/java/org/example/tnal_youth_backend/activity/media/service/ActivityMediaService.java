package org.example.tnal_youth_backend.activity.media.service;

import org.example.tnal_youth_backend.activity.media.dto.response.ActivityAttachmentResponse;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityCoverImageResponse;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityPhotoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ActivityMediaService {

    ActivityCoverImageResponse uploadCoverImage(
            Long activityId,
            MultipartFile file,
            Long currentUserId
    );

    ActivityCoverImageResponse getCoverImage(Long activityId);

    void deleteCoverImage(
            Long activityId,
            Long currentUserId
    );

    List<ActivityPhotoResponse> uploadGalleryImages(
            Long activityId,
            List<MultipartFile> files,
            List<String> captions,
            Long currentUserId
    );

    List<ActivityPhotoResponse> getGalleryImages(Long activityId);

    void deleteGalleryImage(
            Long activityId,
            Long photoId,
            Long currentUserId
    );

    ActivityAttachmentResponse uploadAttachment(
            Long activityId,
            MultipartFile file,
            String title,
            String description,
            Integer sortOrder,
            Long currentUserId
    );

    List<ActivityAttachmentResponse> getAttachments(Long activityId);

    void deleteAttachment(
            Long activityId,
            Long attachmentId,
            Long currentUserId
    );
}
