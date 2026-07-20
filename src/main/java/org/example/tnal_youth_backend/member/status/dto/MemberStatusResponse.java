package org.example.tnal_youth_backend.member.status.dto;

import java.time.OffsetDateTime;

public record MemberStatusResponse(
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