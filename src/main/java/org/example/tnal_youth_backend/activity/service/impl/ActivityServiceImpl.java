package org.example.tnal_youth_backend.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.mapper.ActivityMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityListItemResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityPageResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.repository.ActivitySectorRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityStatusRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityTypeRepository;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivitySectorRepository activitySectorRepository;
    private final ActivityStatusRepository activityStatusRepository;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public ActivityResponse createActivity(
            CreateActivityRequest request,
            Long currentUserId
    ) {
        validateRequest(request);

        ActivityType activityType =
                activityTypeRepository.findById(request.getTypeId())
                        .filter(type ->
                                Boolean.TRUE.equals(type.getActive())
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Activity type is invalid or inactive"
                                )
                        );

        ActivitySector activitySector =
                activitySectorRepository.findById(request.getSectorId())
                        .filter(sector ->
                                Boolean.TRUE.equals(sector.getActive())
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Activity sector is invalid or inactive"
                                )
                        );

        ActivityStatus activityStatus =
                resolveInitialStatus(request);

        /*
         * INTERNAL:
         * - members of the activity branch see it automatically
         * - only same-branch members can be invited individually
         *
         * EXTERNAL:
         * - members of the activity branch see it automatically
         * - another branch can be invited later
         *
         * Neither activity type is public to everyone automatically.
         */
        boolean publicActivity = false;

        Activity activity = activityMapper.toEntity(
                request,
                activityType,
                activitySector,
                activityStatus,
                publicActivity,
                currentUserId
        );

        Activity savedActivity =
                activityRepository.save(activity);

        return activityMapper.toResponse(savedActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(
            Long activityId
    ) {
        Activity activity =
                activityRepository.findById(activityId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Activity not found"
                                )
                        );

        return activityMapper.toResponse(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityPageResponse getActivities(
            int page,
            int size,
            String search,
            Short sectorId,
            Short typeId,
            LocalDate date
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "startsAt"
                )
        );

        Page<Activity> activityPage =
                activityRepository.findAll(pageable);

        List<ActivityListItemResponse> content =
                activityPage.getContent()
                        .stream()
                        .map(activityMapper::toListItemResponse)
                        .toList();

        return ActivityPageResponse.builder()
                .content(content)
                .page(activityPage.getNumber())
                .size(activityPage.getSize())
                .totalElements(activityPage.getTotalElements())
                .totalPages(activityPage.getTotalPages())
                .first(activityPage.isFirst())
                .last(activityPage.isLast())
                .build();
    }

    private void validateRequest(
            CreateActivityRequest request
    ) {
        if (request.getStartsAt() == null
                || request.getEndsAt() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity start and end times are required"
            );
        }

        if (!request.getEndsAt()
                .isAfter(request.getStartsAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity end time must be later than start time"
            );
        }

        if (request.getCommuneId() != null
                && request.getDistrictId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "District is required when commune is selected"
            );
        }

        if (request.getDistrictId() != null
                && request.getProvinceId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Province is required when district is selected"
            );
        }

        if (request.getCapacity() != null
                && request.getCapacity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity capacity must be greater than zero"
            );
        }

        if (request.getTitleKm() == null
                || request.getTitleKm().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khmer activity title is required"
            );
        }

        if (currentStringLength(request.getTitleKm()) > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khmer activity title is too long"
            );
        }

        if (currentStringLength(request.getTitleEn()) > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "English activity title is too long"
            );
        }
    }

    private ActivityStatus resolveInitialStatus(
            CreateActivityRequest request
    ) {
        ActivityStatus requestedStatus =
                activityStatusRepository
                        .findById(request.getStatusId())
                        .filter(status ->
                                Boolean.TRUE.equals(
                                        status.getActive()
                                )
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Activity status is invalid or inactive"
                                )
                        );

        String statusCode =
                requestedStatus.getCode();

        OffsetDateTime now =
                OffsetDateTime.now();

        /*
         * Draft activities remain draft regardless of dates.
         */
        if ("DRAFT".equalsIgnoreCase(statusCode)) {
            return requestedStatus;
        }

        /*
         * Do not allow creating an activity as completed.
         * Completion must be confirmed manually later.
         */
        if ("COMPLETED".equalsIgnoreCase(statusCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity cannot be created as completed"
            );
        }

        /*
         * If UPCOMING is selected but the start time has already arrived,
         * save it as ONGOING.
         */
        if ("UPCOMING".equalsIgnoreCase(statusCode)
                && !request.getStartsAt().isAfter(now)) {

            return activityStatusRepository
                    .findByCodeIgnoreCase("ONGOING")
                    .filter(status ->
                            Boolean.TRUE.equals(
                                    status.getActive()
                            )
                    )
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "ONGOING activity status is not configured"
                            )
                    );
        }

        /*
         * A future activity cannot start as ONGOING.
         */
        if ("ONGOING".equalsIgnoreCase(statusCode)
                && request.getStartsAt().isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A future activity cannot be created as ongoing"
            );
        }

        return requestedStatus;
    }

    private int currentStringLength(
            String value
    ) {
        return value == null
                ? 0
                : value.trim().length();
    }
}