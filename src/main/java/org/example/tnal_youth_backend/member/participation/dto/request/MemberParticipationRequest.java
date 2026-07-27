package org.example.tnal_youth_backend.member.participation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record MemberParticipationRequest(

        @JsonProperty("activity_id")
        @NotNull(message = "Activity ID is required")
        Long activityId,

        @JsonProperty("attendance_status_id")
        Short attendanceStatusId,

        @JsonProperty("registered_at")
        OffsetDateTime registeredAt,

        @JsonProperty("checked_in_at")
        OffsetDateTime checkedInAt,

        @JsonProperty("invited_by")
        Long invitedById,

        String note
) {
}