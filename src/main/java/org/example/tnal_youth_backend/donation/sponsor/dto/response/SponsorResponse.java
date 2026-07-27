package org.example.tnal_youth_backend.donation.sponsor.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record SponsorResponse(

        Long id,

        @JsonProperty("sponsor_type_id")
        Short sponsorTypeId,

        @JsonProperty("sponsor_type_code")
        String sponsorTypeCode,

        @JsonProperty("sponsor_type_label_km")
        String sponsorTypeLabelKm,

        @JsonProperty("sponsor_type_label_en")
        String sponsorTypeLabelEn,

        String name,

        String phone,

        String email,

        String address,

        String note,

        @JsonProperty("is_active")
        Boolean isActive,

        @JsonProperty("created_by")
        Long createdById,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}