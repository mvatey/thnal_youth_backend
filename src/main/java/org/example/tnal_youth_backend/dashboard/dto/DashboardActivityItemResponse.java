package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record DashboardActivityItemResponse(
        Long id,
        String titleKm,
        String titleEn,
        String coverImage,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String locationName,
        String type,
        Long participantCount
) {
}