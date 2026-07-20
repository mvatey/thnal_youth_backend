package org.example.tnal_youth_backend.activity.activity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record ActivityResponse(

        Long id,

        @JsonProperty("title_km")
        String titleKm,

        @JsonProperty("title_en")
        String titleEn,

        String description,

        @JsonProperty("type_id")
        Short typeId,

        @JsonProperty("sector_id")
        Short sectorId,

        @JsonProperty("status_id")
        Short statusId,

        @JsonProperty("branch_id")
        Long branchId,

        @JsonProperty("is_public")
        Boolean isPublic,

        @JsonProperty("starts_at")
        OffsetDateTime startsAt,

        @JsonProperty("ends_at")
        OffsetDateTime endsAt,

        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("district_id")
        Integer districtId,

        @JsonProperty("commune_id")
        Integer communeId,

        @JsonProperty("location_name")
        String locationName,

        String address,

        @JsonProperty("google_map_url")
        String googleMapUrl,

        Integer capacity,

        @JsonProperty("cover_image_id")
        Long coverImageId,

        @JsonProperty("created_by")
        Long createdById,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}