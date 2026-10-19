package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record DashboardActivityItemResponse(
        Long id,
        String titleKm,
        String titleEn,
        // The cover image's FILE ID, not a raw storage path -- see
        // DashboardActivityRow#coverImageId. The frontend builds the
        // actual image URL from this id via GET /api/files/{id}/content,
        // matching how every other activity view (e.g. the activity
        // detail page) resolves coverImageId into a displayable image.
        Long coverImageId,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String locationName,
        String type,
        // The type lookup's current labels (so a rename on the variable
        // page shows here); null for a type that is no longer active.
        String typeLabelKm,
        String typeLabelEn,
        Long participantCount
) {
}