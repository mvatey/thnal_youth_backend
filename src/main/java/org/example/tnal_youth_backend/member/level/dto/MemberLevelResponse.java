package org.example.tnal_youth_backend.member.level.dto;

import java.time.OffsetDateTime;

public record MemberLevelResponse(
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