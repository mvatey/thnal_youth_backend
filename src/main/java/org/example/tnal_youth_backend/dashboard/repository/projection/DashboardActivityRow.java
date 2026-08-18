package org.example.tnal_youth_backend.dashboard.repository.projection;

import java.time.OffsetDateTime;

public record DashboardActivityRow(
        Long id,
        String titleKm,
        String titleEn,
        // The activity's cover image FILE ID (activities.cover_image_id),
        // not a raw storage path -- the frontend builds the actual
        // displayable URL from this via GET /api/files/{id}/content, same
        // as everywhere else in the app a cover image is shown (see
        // ActivityResponse#coverImageId). Used to be the file's raw
        // file_path, which the browser can't load directly since files
        // are only ever served by id, never by path.
        Long coverImageId,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String locationName,
        String type,
        Long participantCount
) {
}