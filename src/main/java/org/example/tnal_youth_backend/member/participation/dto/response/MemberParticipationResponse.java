package org.example.tnal_youth_backend.member.participation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record MemberParticipationResponse(

        Long id,

        @JsonProperty("activity_id")
        Long activityId,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("activity_title_km")
        String activityTitleKm,

        @JsonProperty("activity_title_en")
        String activityTitleEn,

        SectorResponse sector,

        TypeResponse type,

        @JsonProperty("attendance_status")
        AttendanceStatusResponse attendanceStatus,

        LocationResponse location,

        @JsonProperty("starts_at")
        OffsetDateTime startsAt,

        @JsonProperty("ends_at")
        OffsetDateTime endsAt,

        @JsonProperty("registered_at")
        OffsetDateTime registeredAt,

        @JsonProperty("checked_in_at")
        OffsetDateTime checkedInAt,

        @JsonProperty("checked_out_at")
        OffsetDateTime checkedOutAt,

        @JsonProperty("invited_by")
        Long invitedById,

        String note
) {

        public record SectorResponse(
                Short id,
                String code,

                @JsonProperty("label_km")
                String labelKm,

                @JsonProperty("label_en")
                String labelEn
        ) {
        }

        public record TypeResponse(
                Short id,
                String code,

                @JsonProperty("label_km")
                String labelKm,

                @JsonProperty("label_en")
                String labelEn
        ) {
        }

        public record AttendanceStatusResponse(
                Short id,
                String code,

                @JsonProperty("label_km")
                String labelKm,

                @JsonProperty("label_en")
                String labelEn
        ) {
        }

        public record LocationResponse(
                String name,
                String address
        ) {
        }
}