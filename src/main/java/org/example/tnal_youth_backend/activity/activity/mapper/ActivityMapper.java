package org.example.tnal_youth_backend.activity.activity.mapper;

import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityResponse toResponse(Activity activity) {
        if (activity == null) {
            return null;
        }

        return new ActivityResponse(
                activity.getId(),
                activity.getTitleKm(),
                activity.getTitleEn(),
                activity.getDescription(),
                activity.getTypeId(),
                activity.getSectorId(),
                activity.getStatusId(),
                activity.getBranchId(),
                activity.getIsPublic(),
                activity.getStartsAt(),
                activity.getEndsAt(),
                activity.getProvinceId(),
                activity.getDistrictId(),
                activity.getCommuneId(),
                activity.getLocationName(),
                activity.getAddress(),
                activity.getGoogleMapUrl(),
                activity.getCapacity(),
                activity.getCoverImageId(),
                activity.getCreatedById(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }
}