package org.example.tnal_youth_backend.donation.type.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record DonationTypeResponse(

        Short id,

        String code,

        @JsonProperty("label_km")
        String labelKm,

        @JsonProperty("label_en")
        String labelEn,

        String description,

        @JsonProperty("is_active")
        Boolean isActive,

        @JsonProperty("sort_order")
        Short sortOrder,

        @JsonProperty("created_at")
        OffsetDateTime createdAt
) {
}