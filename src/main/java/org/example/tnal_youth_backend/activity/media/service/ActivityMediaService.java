package org.example.tnal_youth_backend.activity.media.service;

import org.example.tnal_youth_backend.activity.media.dto.response.ActivityCoverImageResponse;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityPhotoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ActivityMediaService {

    // ============================================================
    // COVER IMAGE
    // ============================================================

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

    // ============================================================
    // GALLERY IMAGES
    // ============================================================

    /**
     * Upload multiple gallery images for one activity.
     *
     * One caption may be supplied for each uploaded image.
     * Captions are optional.
     */
    List<ActivityPhotoResponse> uploadGalleryImages(
            Long activityId,
            List<MultipartFile> files,
            List<String> captions,
            Long currentUserId
    );

    /**
     * Return all gallery images for one activity.
     */
    List<ActivityPhotoResponse> getGalleryImages(
            Long activityId
    );

    /**
     * Delete one gallery photo from an activity.
     */
    void deleteGalleryImage(
            Long activityId,
            Long photoId,
            Long currentUserId
    );
}