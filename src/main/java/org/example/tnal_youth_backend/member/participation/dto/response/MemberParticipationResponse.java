package org.example.tnal_youth_backend.member.participation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record MemberParticipationResponse(

        Long id,

        @JsonProperty("activity_id")
        Long activityId,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("attendance_status_id")
        Short attendanceStatusId,

        @JsonProperty("registered_at")
        OffsetDateTime registeredAt,

        @JsonProperty("checked_in_at")
        OffsetDateTime checkedInAt,

        @JsonProperty("invited_by")
        Long invitedById,

        String note,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}