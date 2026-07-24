package org.example.tnal_youth_backend.account.memberactivity.dto.response;

import java.time.OffsetDateTime;

public record MyActivityResponse(

        Long activityId,

        String titleKm,

        String titleEn,

        String description,

        OffsetDateTime startsAt,

        OffsetDateTime endsAt,

        String locationName,

        String address,

        Boolean isPublic,

        Integer capacity,

        Long activityTypeId,

        String activityTypeCode,

        String activityTypeLabelKm,

        String activityTypeLabelEn,

        Long activitySectorId,

        String activitySectorCode,

        String activitySectorLabelKm,

        String activitySectorLabelEn,

        Long activityStatusId,

        String activityStatusCode,

        String activityStatusLabelKm,

        String activityStatusLabelEn,

        Long branchId,

        String branchNameKm,

        String branchNameEn,

        Long participantId,

        Boolean invited,

        OffsetDateTime joinedAt,

        Long attendanceStatusId,

        String attendanceStatusCode,

        String attendanceStatusLabelKm,

        String attendanceStatusLabelEn,

        Long coverImageId,

        String coverImagePath,

        String coverImageOriginalName
) {
}