package org.example.tnal_youth_backend.activity.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttendanceStatusResponse(

        Short id,

        String code,

        @JsonProperty("label_km")
        String labelKm,

        @JsonProperty("label_en")
        String labelEn,

        String description,

        @JsonProperty("sort_order")
        Integer sortOrder
) {
}