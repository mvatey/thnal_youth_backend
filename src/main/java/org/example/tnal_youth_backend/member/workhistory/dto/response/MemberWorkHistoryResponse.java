package org.example.tnal_youth_backend.member.workhistory.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MemberWorkHistoryResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("organization_name")
        String organizationName,

        @JsonProperty("position_title")
        String positionTitle,

        @JsonProperty("role_title")
        String roleTitle,

        String address,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}