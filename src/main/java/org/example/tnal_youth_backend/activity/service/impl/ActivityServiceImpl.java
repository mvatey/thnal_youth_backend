package org.example.tnal_youth_backend.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.mapper.ActivityMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.repository.ActivitySectorRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityStatusRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityTypeRepository;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                activityStatusRepository.findById(request.getStatusId())
                        .filter(status ->
                                Boolean.TRUE.equals(status.getActive())
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Activity status is invalid or inactive"
                                )
                        );

        Activity activity = activityMapper.toEntity(
                request,
                activityType,
                activitySector,
                activityStatus,
                currentUserId
        );

        Activity savedActivity =
                activityRepository.save(activity);

        return activityMapper.toResponse(savedActivity);
    }

    private void validateRequest(
            CreateActivityRequest request
    ) {
        if (!request.getEndsAt().isAfter(request.getStartsAt())) {
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

        if (currentStringLength(request.getTitleKm()) > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khmer activity title is too long"
            );
        }
    }

    private int currentStringLength(String value) {
        return value == null
                ? 0
                : value.trim().length();
    }
}