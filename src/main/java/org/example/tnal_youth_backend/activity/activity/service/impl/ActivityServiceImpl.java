package org.example.tnal_youth_backend.activity.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.activity.dto.request.ActivityRequest;
import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.example.tnal_youth_backend.activity.activity.mapper.ActivityMapper;
import org.example.tnal_youth_backend.activity.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.activity.service.ActivityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getAllActivities() {

        return activityRepository
                .findAllByOrderByStartsAtDescIdDesc()
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> searchActivities(
            String search
    ) {
        String normalizedSearch = trimToNull(search);

        if (normalizedSearch == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search value is required"
            );
        }

        return activityRepository
                .searchByTitle(normalizedSearch)
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> filterActivitiesByType(
            Short typeId
    ) {
        if (typeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity type ID is required"
            );
        }

        return activityRepository
                .findAllByTypeIdOrderByStartsAtDescIdDesc(
                        typeId
                )
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(Long id) {
        return activityMapper.toResponse(
                findActivity(id)
        );
    }

    @Override
    @Transactional
    public ActivityResponse createActivity(
            ActivityRequest request
    ) {
        validateRequest(request);

        Activity activity = Activity.builder()
                .titleKm(
                        normalizeRequired(
                                request.titleKm(),
                                "Khmer activity title"
                        )
                )
                .titleEn(
                        trimToNull(request.titleEn())
                )
                .description(
                        trimToNull(request.description())
                )
                .typeId(request.typeId())
                .sectorId(request.sectorId())
                .statusId(request.statusId())
                .branchId(request.branchId())
                .isPublic(
                        request.isPublic() != null
                                ? request.isPublic()
                                : false
                )
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .provinceId(request.provinceId())
                .districtId(request.districtId())
                .communeId(request.communeId())
                .locationName(
                        trimToNull(request.locationName())
                )
                .address(
                        trimToNull(request.address())
                )
                .googleMapUrl(
                        trimToNull(request.googleMapUrl())
                )
                .capacity(request.capacity())
                .coverImageId(request.coverImageId())
                .createdById(request.createdById())
                .build();

        try {
            Activity saved =
                    activityRepository.saveAndFlush(activity);

            return activityMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException(exception);
        }
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(
            Long id,
            ActivityRequest request
    ) {
        Activity activity = findActivity(id);

        validateRequest(request);

        activity.setTitleKm(
                normalizeRequired(
                        request.titleKm(),
                        "Khmer activity title"
                )
        );

        activity.setTitleEn(
                trimToNull(request.titleEn())
        );

        activity.setDescription(
                trimToNull(request.description())
        );

        activity.setTypeId(request.typeId());
        activity.setSectorId(request.sectorId());
        activity.setStatusId(request.statusId());
        activity.setBranchId(request.branchId());

        activity.setIsPublic(
                request.isPublic() != null
                        ? request.isPublic()
                        : false
        );

        activity.setStartsAt(request.startsAt());
        activity.setEndsAt(request.endsAt());
        activity.setProvinceId(request.provinceId());
        activity.setDistrictId(request.districtId());
        activity.setCommuneId(request.communeId());

        activity.setLocationName(
                trimToNull(request.locationName())
        );

        activity.setAddress(
                trimToNull(request.address())
        );

        activity.setGoogleMapUrl(
                trimToNull(request.googleMapUrl())
        );

        activity.setCapacity(request.capacity());

        activity.setCoverImageId(
                request.coverImageId()
        );

        /*
         * createdById is intentionally not updated.
         * It records the original creator of the activity.
         */

        try {
            Activity updated =
                    activityRepository.saveAndFlush(activity);

            return activityMapper.toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException(exception);
        }
    }

    @Override
    @Transactional
    public void deleteActivity(Long id) {
        Activity activity = findActivity(id);

        try {
            activityRepository.delete(activity);
            activityRepository.flush();

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this activity because it is being \
                    used by participants, expenses, photos, donations, \
                    documents, or another database record.
                    """,
                    exception
            );
        }
    }

    private Activity findActivity(Long id) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is required"
            );
        }

        return activityRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Activity not found with ID: " + id
                        )
                );
    }

    private void validateRequest(
            ActivityRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity request is required"
            );
        }

        if (request.startsAt() != null
                && request.endsAt() != null
                && !request.endsAt()
                .isAfter(request.startsAt())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time"
            );
        }

        if (request.communeId() != null
                && request.districtId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    District ID is required when commune ID \
                    is provided
                    """
            );
        }

        if (request.capacity() != null
                && request.capacity() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Capacity must be greater than zero"
            );
        }
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private ResponseStatusException
    databaseConstraintException(
            DataIntegrityViolationException exception
    ) {
        Throwable mostSpecificCause =
                exception.getMostSpecificCause();

        String databaseMessage =
                mostSpecificCause != null
                        && mostSpecificCause.getMessage() != null
                        ? mostSpecificCause.getMessage()
                        : "Unknown database constraint error";

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Activity could not be saved. Check that type_id, \
                sector_id, status_id, branch_id, province_id, \
                district_id, commune_id, cover_image_id, and \
                created_by reference existing records.

                Database message: %s
                """.formatted(databaseMessage),
                exception
        );
    }
}