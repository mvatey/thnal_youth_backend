package org.example.tnal_youth_backend.activity.type.mapper;

import org.example.tnal_youth_backend.activity.type.dto.response.ActivityTypeResponse;
import org.example.tnal_youth_backend.activity.type.entity.ActivityType;
import org.springframework.stereotype.Component;

@Component
public class ActivityTypeMapper {

    public ActivityTypeResponse toResponse(
            ActivityType activityType
    ) {
        if (activityType == null) {
            return null;
        }

        return new ActivityTypeResponse(
                activityType.getId(),
                activityType.getCode(),
                activityType.getLabelKm(),
                activityType.getLabelEn(),
                activityType.getDescription(),
                activityType.getSortOrder()
        );
    }
}