package org.example.tnal_youth_backend.activity.media.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityCoverImageResponse;
import org.example.tnal_youth_backend.activity.media.service.ActivityMediaService;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ActivityMediaServiceImpl
        implements ActivityMediaService {

    private final ActivityRepository activityRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public ActivityCoverImageResponse uploadCoverImage(
            Long activityId,
            MultipartFile file,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        if (currentUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        Long previousCoverImageId =
                activity.getCoverImageId();

        FileEntity uploadedFile =
                fileService.uploadImage(
                        file,
                        currentUserId
                );

        activity.setCoverImageId(
                uploadedFile.getId()
        );

        try {
            activityRepository.saveAndFlush(activity);

        } catch (RuntimeException exception) {
            /*
             * The file was saved, but linking it to the activity failed.
             * Remove the newly uploaded file to avoid an unused record.
             */
            try {
                fileService.deleteFile(
                        uploadedFile.getId()
                );
            } catch (RuntimeException ignored) {
                // Preserve the original database exception.
            }

            throw exception;
        }

        /*
         * Delete the previous cover only after the new cover
         * has been linked successfully.
         */
        if (previousCoverImageId != null
                && !previousCoverImageId.equals(
                uploadedFile.getId()
        )) {

            try {
                fileService.deleteFile(
                        previousCoverImageId
                );
            } catch (ResponseStatusException exception) {
                /*
                 * The old file may still be referenced somewhere else.
                 * The new cover remains valid, so this does not fail
                 * the replacement operation.
                 */
            }
        }

        return toResponse(
                activity,
                uploadedFile
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityCoverImageResponse getCoverImage(
            Long activityId
    ) {
        Activity activity = findActivity(activityId);

        Long coverImageId =
                activity.getCoverImageId();

        if (coverImageId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This activity does not have a cover image"
            );
        }

        FileEntity file =
                fileService.getFileEntity(
                        coverImageId
                );

        return toResponse(activity, file);
    }

    @Override
    @Transactional
    public void deleteCoverImage(
            Long activityId,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        if (currentUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        Long coverImageId =
                activity.getCoverImageId();

        if (coverImageId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This activity does not have a cover image"
            );
        }

        /*
         * Remove the activity reference first so the file foreign-key
         * reference no longer blocks deletion.
         */
        activity.setCoverImageId(null);
        activityRepository.saveAndFlush(activity);

        try {
            fileService.deleteFile(coverImageId);

        } catch (RuntimeException exception) {
            /*
             * Restore the reference if physical/database deletion fails.
             */
            activity.setCoverImageId(coverImageId);
            activityRepository.saveAndFlush(activity);

            throw exception;
        }
    }

    private Activity findActivity(
            Long activityId
    ) {
        if (activityId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is required"
            );
        }

        return activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Activity not found with ID: "
                                        + activityId
                        )
                );
    }

    private ActivityCoverImageResponse toResponse(
            Activity activity,
            FileEntity file
    ) {
        return ActivityCoverImageResponse.builder()
                .activityId(activity.getId())
                .fileId(file.getId())
                .filePath(file.getFilePath())
                .originalName(file.getOriginalName())
                .mimeType(file.getMimeType())
                .sizeBytes(file.getSizeBytes())
                .downloadUrl(
                        "/api/files/"
                                + file.getId()
                                + "/content"
                )
                .build();
    }
}