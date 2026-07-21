package org.example.tnal_youth_backend.activity.mapper;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityListItemResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public Activity toEntity(
            CreateActivityRequest request,
            ActivityType type,
            ActivitySector sector,
            ActivityStatus status,
            Boolean publicActivity,
            Long createdBy
    ) {
        return Activity.builder()
                .titleKm(trim(request.getTitleKm()))
                .titleEn(trimToNull(request.getTitleEn()))
                .description(trimToNull(request.getDescription()))
                .type(type)
                .sector(sector)
                .status(status)
                .branchId(request.getBranchId())
                .publicActivity(publicActivity)
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .provinceId(request.getProvinceId())
                .districtId(request.getDistrictId())
                .communeId(request.getCommuneId())
                .locationName(trimToNull(request.getLocationName()))
                .address(trimToNull(request.getAddress()))
                .googleMapUrl(trimToNull(request.getGoogleMapUrl()))
                .capacity(request.getCapacity())
                .coverImageId(request.getCoverImageId())
                .createdBy(createdBy)
                .build();
    }

    public ActivityResponse toResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .titleKm(activity.getTitleKm())
                .titleEn(activity.getTitleEn())
                .description(activity.getDescription())
                .type(toLookupResponse(activity.getType()))
                .sector(toLookupResponse(activity.getSector()))
                .status(toLookupResponse(activity.getStatus()))
                .branchId(activity.getBranchId())
                .publicActivity(activity.getPublicActivity())
                .startsAt(activity.getStartsAt())
                .endsAt(activity.getEndsAt())
                .provinceId(activity.getProvinceId())
                .districtId(activity.getDistrictId())
                .communeId(activity.getCommuneId())
                .locationName(activity.getLocationName())
                .address(activity.getAddress())
                .googleMapUrl(activity.getGoogleMapUrl())
                .capacity(activity.getCapacity())
                .coverImageId(activity.getCoverImageId())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }

    private ActivityResponse.LookupResponse toLookupResponse(
            ActivityType type
    ) {
        if (type == null) {
            return null;
        }

        return ActivityResponse.LookupResponse.builder()
                .id(type.getId())
                .code(type.getCode())
                .labelKm(type.getLabelKm())
                .labelEn(type.getLabelEn())
                .build();
    }

    private ActivityResponse.LookupResponse toLookupResponse(
            ActivitySector sector
    ) {
        if (sector == null) {
            return null;
        }

        return ActivityResponse.LookupResponse.builder()
                .id(sector.getId())
                .code(sector.getCode())
                .labelKm(sector.getLabelKm())
                .labelEn(sector.getLabelEn())
                .build();
    }

    private ActivityResponse.LookupResponse toLookupResponse(
            ActivityStatus status
    ) {
        if (status == null) {
            return null;
        }

        return ActivityResponse.LookupResponse.builder()
                .id(status.getId())
                .code(status.getCode())
                .labelKm(status.getLabelKm())
                .labelEn(status.getLabelEn())
                .build();
    }

    private String trim(String value) {
        return value == null
                ? null
                : value.trim();
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

    public ActivityListItemResponse toListItemResponse(
            Activity activity
    ) {
        return ActivityListItemResponse.builder()
                .id(activity.getId())
                .titleKm(activity.getTitleKm())
                .titleEn(activity.getTitleEn())
                .type(toLookupResponse(activity.getType()))
                .sector(toLookupResponse(activity.getSector()))
                .status(toLookupResponse(activity.getStatus()))
                .branchId(activity.getBranchId())
                .publicActivity(activity.getPublicActivity())
                .startsAt(activity.getStartsAt())
                .endsAt(activity.getEndsAt())
                .locationName(activity.getLocationName())
                .capacity(activity.getCapacity())
                .build();
    }
}