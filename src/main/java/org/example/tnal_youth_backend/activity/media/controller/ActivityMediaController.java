package org.example.tnal_youth_backend.activity.media.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityCoverImageResponse;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityPhotoResponse;
import org.example.tnal_youth_backend.activity.media.service.ActivityMediaService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/activities/{activityId}")
@RequiredArgsConstructor
public class ActivityMediaController {

    private final ActivityMediaService activityMediaService;

    // ============================================================
    // COVER IMAGE
    // ============================================================

    @PostMapping(
            value = "/cover-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<ActivityCoverImageResponse> uploadCoverImage(
            @PathVariable Long activityId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        ActivityCoverImageResponse response =
                activityMediaService.uploadCoverImage(
                        activityId,
                        file,
                        getCurrentUserId(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/cover-image")
    public ResponseEntity<ActivityCoverImageResponse> getCoverImage(
            @PathVariable Long activityId
    ) {
        ActivityCoverImageResponse response =
                activityMediaService.getCoverImage(
                        activityId
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cover-image")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<Void> deleteCoverImage(
            @PathVariable Long activityId,
            Authentication authentication
    ) {
        activityMediaService.deleteCoverImage(
                activityId,
                getCurrentUserId(authentication)
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ============================================================
    // GALLERY IMAGES
    // ============================================================

    @PostMapping(
            value = "/gallery",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<List<ActivityPhotoResponse>> uploadGalleryImages(
            @PathVariable Long activityId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(
                    value = "captions",
                    required = false
            )
            List<String> captions,
            Authentication authentication
    ) {
        List<ActivityPhotoResponse> responses =
                activityMediaService.uploadGalleryImages(
                        activityId,
                        files,
                        captions,
                        getCurrentUserId(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }

    @GetMapping("/gallery")
    public ResponseEntity<List<ActivityPhotoResponse>> getGalleryImages(
            @PathVariable Long activityId
    ) {
        List<ActivityPhotoResponse> responses =
                activityMediaService.getGalleryImages(
                        activityId
                );

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/gallery/{photoId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<Void> deleteGalleryImage(
            @PathVariable Long activityId,
            @PathVariable Long photoId,
            Authentication authentication
    ) {
        activityMediaService.deleteGalleryImage(
                activityId,
                photoId,
                getCurrentUserId(authentication)
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ============================================================
    // AUTHENTICATED USER
    // ============================================================

    private Long getCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }
}