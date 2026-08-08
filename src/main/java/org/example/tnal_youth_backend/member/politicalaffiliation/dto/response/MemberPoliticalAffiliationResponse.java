package org.example.tnal_youth_backend.member.politicalaffiliation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MemberPoliticalAffiliationResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("party_id")
        Short partyId,

        @JsonProperty("country")
        String country,

        @JsonProperty("location")
        String location,

        @JsonProperty("position_title")
        String positionTitle,

        @JsonProperty("card_no")
        String cardNo,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        @JsonProperty("is_current")
        Boolean isCurrent,

        @JsonProperty("note")
        String note,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}