package org.example.tnal_youth_backend.member.religion.dto;

import java.time.OffsetDateTime;

public record ReligionResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn,
        String description,
        Boolean isActive,
        Integer sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}