package org.example.tnal_youth_backend.dashboard.dto;

import java.time.OffsetDateTime;

public record DashboardActivityResponse(
        Long id,
        String titleKm,
        String titleEn,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String imageUrl,
        ActivityTypeInfo type,
        ActivitySectorInfo sector,
        Long participantCount
) {

    public record ActivityTypeInfo(
            Short id,
            String code,
            String labelKm,
            String labelEn
    ) {
    }

    public record ActivitySectorInfo(
            Short id,
            String code,
            String labelKm,
            String labelEn
    ) {
    }
}