package org.example.tnal_youth_backend.dashboard.repository.projection;

import java.time.OffsetDateTime;

public record DashboardActivityRow(
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