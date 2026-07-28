package org.example.tnal_youth_backend.activity.type.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActivityTypeResponse(

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