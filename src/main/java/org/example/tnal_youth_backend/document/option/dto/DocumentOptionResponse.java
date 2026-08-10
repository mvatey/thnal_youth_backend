package org.example.tnal_youth_backend.document.option.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DocumentOptionResponse(
        Short id,
        String category,
        String code,
        String value,
        @JsonProperty("label_km") String labelKm,
        @JsonProperty("label_en") String labelEn,
        String description,
        @JsonProperty("sort_order") Integer sortOrder
) {
}
