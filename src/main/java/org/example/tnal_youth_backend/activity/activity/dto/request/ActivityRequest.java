package org.example.tnal_youth_backend.activity.activity.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record ActivityRequest(

        @JsonProperty("title_km")
        @NotBlank(message = "Khmer activity title is required")
        @Size(
                max = 255,
                message = "Khmer activity title must not exceed 255 characters"
        )
        String titleKm,

        @JsonProperty("title_en")
        @Size(
                max = 255,
                message = "English activity title must not exceed 255 characters"
        )
        String titleEn,

        String description,

        @JsonProperty("type_id")
        @NotNull(message = "Activity type ID is required")
        Short typeId,

        @JsonProperty("sector_id")
        @NotNull(message = "Activity sector ID is required")
        Short sectorId,

        @JsonProperty("status_id")
        @NotNull(message = "Activity status ID is required")
        Short statusId,

        @JsonProperty("branch_id")
        @NotNull(message = "Branch ID is required")
        Long branchId,

        @JsonProperty("is_public")
        Boolean isPublic,

        @JsonProperty("starts_at")
        @NotNull(message = "Start time is required")
        OffsetDateTime startsAt,

        @JsonProperty("ends_at")
        @NotNull(message = "End time is required")
        OffsetDateTime endsAt,

        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("district_id")
        Integer districtId,

        @JsonProperty("commune_id")
        Integer communeId,

        @JsonProperty("location_name")
        @Size(
                max = 255,
                message = "Location name must not exceed 255 characters"
        )
        String locationName,

        String address,

        @JsonProperty("google_map_url")
        String googleMapUrl,

        @Positive(message = "Capacity must be greater than zero")
        Integer capacity,

        @JsonProperty("cover_image_id")
        Long coverImageId,

        @JsonProperty("created_by")
        @NotNull(message = "Created-by user ID is required")
        Long createdById
) {

        @AssertTrue(
                message = "End time must be after start time"
        )
        public boolean isTimeRangeValid() {
                return startsAt == null
                        || endsAt == null
                        || endsAt.isAfter(startsAt);
        }

        @AssertTrue(
                message = "District ID is required when commune ID is provided"
        )
        public boolean isLocationSelectionValid() {
                return communeId == null || districtId != null;
        }
}