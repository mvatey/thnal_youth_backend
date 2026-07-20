package org.example.tnal_youth_backend.activity.status.mapper;

import org.example.tnal_youth_backend.activity.status.dto.response.ActivityStatusResponse;
import org.example.tnal_youth_backend.activity.status.entity.ActivityStatus;
import org.springframework.stereotype.Component;

@Component
public class ActivityStatusMapper {

    public ActivityStatusResponse toResponse(
            ActivityStatus activityStatus
    ) {
        if (activityStatus == null) {
            return null;
        }

        return new ActivityStatusResponse(
                activityStatus.getId(),
                activityStatus.getCode(),
                activityStatus.getLabelKm(),
                activityStatus.getLabelEn(),
                activityStatus.getDescription(),
                activityStatus.getSortOrder()
        );
    }
}