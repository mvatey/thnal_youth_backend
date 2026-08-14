package org.example.tnal_youth_backend.activity.media.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityAttachmentResponse;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityCoverImageResponse;
import org.example.tnal_youth_backend.activity.media.dto.response.ActivityPhotoResponse;
import org.example.tnal_youth_backend.activity.media.entity.ActivityAttachment;
import org.example.tnal_youth_backend.activity.media.entity.ActivityPhoto;
import org.example.tnal_youth_backend.activity.media.repository.ActivityAttachmentRepository;
import org.example.tnal_youth_backend.activity.media.repository.ActivityPhotoRepository;
import org.example.tnal_youth_backend.activity.media.service.ActivityMediaService;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityMediaServiceImpl implements ActivityMediaService {

    private final ActivityRepository activityRepository;
    private final ActivityPhotoRepository activityPhotoRepository;
    private final ActivityAttachmentRepository activityAttachmentRepository;
    private final FileService fileService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;

    // ============================================================
    // COVER IMAGE
    // ============================================================

    @Override
    @Transactional
    public ActivityCoverImageResponse uploadCoverImage(
            Long activityId,
            MultipartFile file,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        validateManagePermission(activity, currentUserId);

        Long previousCoverImageId = activity.getCoverImageId();

        FileEntity uploadedFile = fileService.uploadImage(
                file,
                currentUserId
        );

        activity.setCoverImageId(uploadedFile.getId());

        try {
            activityRepository.saveAndFlush(activity);
        } catch (RuntimeException exception) {
            try {
                fileService.deleteFile(uploadedFile.getId());
            } catch (RuntimeException ignored) {
                // Preserve the original exception.
            }

            throw exception;
        }

        if (previousCoverImageId != null
                && !previousCoverImageId.equals(uploadedFile.getId())) {

            try {
                fileService.deleteFile(previousCoverImageId);
            } catch (ResponseStatusException ignored) {
                /*
                 * The old file may still be referenced elsewhere.
                 * The new cover image remains valid.
                 */
            }
        }

        return toCoverResponse(activity, uploadedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityCoverImageResponse getCoverImage(
            Long activityId
    ) {
        Activity activity = findActivity(activityId);

        Long coverImageId = activity.getCoverImageId();

        if (coverImageId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This activity does not have a cover image"
            );
        }

        FileEntity file = fileService.getFileEntity(coverImageId);

        return toCoverResponse(activity, file);
    }

    @Override
    @Transactional
    public void deleteCoverImage(
            Long activityId,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        validateManagePermission(activity, currentUserId);

        Long coverImageId = activity.getCoverImageId();

        if (coverImageId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This activity does not have a cover image"
            );
        }

        activity.setCoverImageId(null);
        activityRepository.saveAndFlush(activity);

        try {
            fileService.deleteFile(coverImageId);
        } catch (RuntimeException exception) {
            activity.setCoverImageId(coverImageId);
            activityRepository.saveAndFlush(activity);

            throw exception;
        }
    }

    // ============================================================
    // GALLERY IMAGES
    // ============================================================

    @Override
    @Transactional
    public List<ActivityPhotoResponse> uploadGalleryImages(
            Long activityId,
            List<MultipartFile> files,
            List<String> captions,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        validateManagePermission(activity, currentUserId);
        validateGalleryFiles(files);

        int nextSortOrder = activityPhotoRepository
                .findTopByActivityIdOrderBySortOrderDesc(activityId)
                .map(ActivityPhoto::getSortOrder)
                .orElse(-1) + 1;

        /*
         * The activity gallery has no photos yet and the activity does not
         * already have a cover image, so the first photo uploaded in this
         * batch becomes the activity's cover ("main picture").
         */
        boolean shouldAssignCover = nextSortOrder == 0
                && activity.getCoverImageId() == null;

        List<ActivityPhotoResponse> responses = new ArrayList<>();
        List<Long> uploadedFileIds = new ArrayList<>();

        try {
            for (int index = 0; index < files.size(); index++) {
                MultipartFile multipartFile = files.get(index);

                FileEntity uploadedFile = fileService.uploadImage(
                        multipartFile,
                        currentUserId
                );

                uploadedFileIds.add(uploadedFile.getId());

                if (activityPhotoRepository.existsByActivityIdAndFileId(
                        activityId,
                        uploadedFile.getId()
                )) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "This file is already linked to the activity gallery"
                    );
                }

                if (shouldAssignCover && index == 0) {
                    activity.setCoverImageId(uploadedFile.getId());
                    activityRepository.saveAndFlush(activity);
                }

                String caption = getCaption(captions, index);

                ActivityPhoto activityPhoto = ActivityPhoto.builder()
                        .activity(activity)
                        .file(uploadedFile)
                        .caption(caption)
                        .sortOrder(nextSortOrder++)
                        .uploadedBy(currentUserId)
                        .build();

                ActivityPhoto savedPhoto =
                        activityPhotoRepository.saveAndFlush(activityPhoto);

                responses.add(
                        toPhotoResponse(savedPhoto)
                );
            }

            return responses;

        } catch (RuntimeException exception) {
            /*
             * Clean up uploaded files if gallery database saving fails.
             */
            for (Long fileId : uploadedFileIds) {
                try {
                    fileService.deleteFile(fileId);
                } catch (RuntimeException ignored) {
                    // Preserve the original exception.
                }
            }

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityPhotoResponse> getGalleryImages(
            Long activityId
    ) {
        findActivity(activityId);

        return activityPhotoRepository
                .findByActivityIdOrderBySortOrderAscIdAsc(activityId)
                .stream()
                .map(this::toPhotoResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteGalleryImage(
            Long activityId,
            Long photoId,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        validateManagePermission(activity, currentUserId);

        if (photoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Photo ID is required"
            );
        }

        ActivityPhoto activityPhoto =
                activityPhotoRepository
                        .findByIdAndActivityId(photoId, activityId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Gallery image not found with ID: "
                                                + photoId
                                )
                        );

        Long fileId = activityPhoto.getFile().getId();

        /*
         * Remove the gallery record first so its foreign-key reference
         * no longer blocks deletion from the files table.
         */
        activityPhotoRepository.delete(activityPhoto);
        activityPhotoRepository.flush();

        try {
            fileService.deleteFile(fileId);
        } catch (RuntimeException exception) {
            /*
             * The surrounding transaction will roll back the gallery
             * deletion if deleting the stored file fails.
             */
            throw exception;
        }
    }

    // ============================================================
    // ATTACHMENTS
    // ============================================================

    @Override
    @Transactional
    public ActivityAttachmentResponse uploadAttachment(
            Long activityId,
            MultipartFile file,
            String title,
            String description,
            Integer sortOrder,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        validateManagePermission(activity, currentUserId);
        validateAttachmentFile(file);

        int resolvedSortOrder = sortOrder == null
                ? activityAttachmentRepository
                  .findTopByActivityIdOrderBySortOrderDescIdDesc(activityId)
                  .map(ActivityAttachment::getSortOrder)
                  .orElse(-1) + 1
                : sortOrder;

        if (resolvedSortOrder < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attachment sort order must be zero or greater"
            );
        }

        FileEntity uploadedFile = fileService.uploadAttachment(
                file,
                currentUserId
        );

        try {
            if (activityAttachmentRepository.existsByActivityIdAndFileId(
                    activityId,
                    uploadedFile.getId()
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This file is already attached to the activity"
                );
            }

            ActivityAttachment attachment = ActivityAttachment.builder()
                    .activityId(activity.getId())
                    .fileId(uploadedFile.getId())
                    .title(normalizeText(title))
                    .description(normalizeText(description))
                    .sortOrder(resolvedSortOrder)
                    .uploadedBy(currentUserId)
                    .build();

            ActivityAttachment savedAttachment =
                    activityAttachmentRepository.saveAndFlush(attachment);

            return toAttachmentResponse(savedAttachment);
        } catch (RuntimeException exception) {
            try {
                fileService.deleteFile(uploadedFile.getId());
            } catch (RuntimeException ignored) {
                // Preserve the original exception.
            }

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityAttachmentResponse> getAttachments(
            Long activityId
    ) {
        findActivity(activityId);

        return activityAttachmentRepository
                .findByActivityIdOrderBySortOrderAscIdAsc(activityId)
                .stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAttachment(
            Long activityId,
            Long attachmentId,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        validateManagePermission(activity, currentUserId);

        if (attachmentId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attachment ID is required"
            );
        }

        ActivityAttachment attachment = activityAttachmentRepository
                .findByIdAndActivityId(attachmentId, activityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment not found with ID: " + attachmentId
                ));

        Long fileId = attachment.getFileId();

        activityAttachmentRepository.delete(attachment);
        activityAttachmentRepository.flush();

        fileService.deleteFile(fileId);
    }

    // ============================================================
    // HELPERS
    // ============================================================

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with ID: " + activityId
                ));
    }

    /**
     * Only a branch leader or secretary who is staff of this activity's own
     * host branch may upload/delete the activity's cover image, gallery
     * photos, or attachments.
     */
    private void validateManagePermission(
            Activity activity,
            Long currentUserId
    ) {
        if (currentUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        if (currentUser.getRole() != UserRole.BRANCH_LEADER
                && currentUser.getRole() != UserRole.SECRETARY) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a branch leader or secretary can manage "
                            + "activity files"
            );
        }

        Set<Long> staffBranchIds = resolveStaffBranchIds(currentUser);

        if (activity.getBranchId() == null
                || !staffBranchIds.contains(activity.getBranchId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only manage files for activities hosted "
                            + "by your own branch"
            );
        }
    }

    private Set<Long> resolveStaffBranchIds(User user) {
        if (user.getMemberId() == null) {
            return Set.of();
        }

        Set<Long> branchIds = new LinkedHashSet<>(
                branchStaffRepository.findActiveBranchIdsByMemberId(
                        user.getMemberId()
                )
        );

        memberRepository.findById(user.getMemberId())
                .map(Member::getBranchId)
                .ifPresent(branchIds::add);

        return branchIds;
    }

    private void validateGalleryFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one gallery image is required"
            );
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gallery images must not be empty"
                );
            }
        }
    }

    private void validateAttachmentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attachment file is required"
            );
        }
    }

    private String getCaption(List<String> captions, int index) {
        if (captions == null || index >= captions.size()) {
            return null;
        }

        return normalizeText(captions.get(index));
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ActivityCoverImageResponse toCoverResponse(
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
                .downloadUrl("/api/files/" + file.getId() + "/content")
                .build();
    }

    private ActivityPhotoResponse toPhotoResponse(
            ActivityPhoto activityPhoto
    ) {
        FileEntity file = activityPhoto.getFile();

        return ActivityPhotoResponse.builder()
                .photoId(activityPhoto.getId())
                .activityId(activityPhoto.getActivity().getId())
                .fileId(file.getId())
                .caption(activityPhoto.getCaption())
                .sortOrder(activityPhoto.getSortOrder())
                .filePath(file.getFilePath())
                .originalName(file.getOriginalName())
                .mimeType(file.getMimeType())
                .sizeBytes(file.getSizeBytes())
                .downloadUrl("/api/files/" + file.getId() + "/content")
                .build();
    }

    private ActivityAttachmentResponse toAttachmentResponse(
            ActivityAttachment attachment
    ) {
        FileEntity file = fileService.getFileEntity(attachment.getFileId());

        return ActivityAttachmentResponse.builder()
                .attachmentId(attachment.getId())
                .activityId(attachment.getActivityId())
                .fileId(file.getId())
                .title(attachment.getTitle())
                .description(attachment.getDescription())
                .sortOrder(attachment.getSortOrder())
                .uploadedBy(attachment.getUploadedBy())
                .uploadedAt(attachment.getUploadedAt())
                .filePath(file.getFilePath())
                .originalName(file.getOriginalName())
                .mimeType(file.getMimeType())
                .sizeBytes(file.getSizeBytes())
                .downloadUrl("/api/files/" + file.getId() + "/content")
                .build();
    }
}
