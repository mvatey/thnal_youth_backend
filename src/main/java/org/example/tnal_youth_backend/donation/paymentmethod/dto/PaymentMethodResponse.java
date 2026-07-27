package org.example.tnal_youth_backend.donation.paymentmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record PaymentMethodResponse(

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
        Integer sortOrder,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}