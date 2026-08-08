package org.example.tnal_youth_backend.member.politicalaffiliation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberPoliticalAffiliationRequest(

        @JsonProperty("party_id")
        @NotNull(
                message = "Political party is required"
        )
        Short partyId,

        @JsonProperty("country")
        @Size(
                max = 100,
                message = "Country must not exceed 100 characters"
        )
        String country,

        @JsonProperty("location")
        @Size(
                max = 255,
                message = "Location must not exceed 255 characters"
        )
        String location,

        @JsonProperty("position_title")
        @Size(
                max = 255,
                message = "Position title must not exceed 255 characters"
        )
        String positionTitle,

        @JsonProperty("card_no")
        @Size(
                max = 100,
                message = "Card number must not exceed 100 characters"
        )
        String cardNo,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        @JsonProperty("is_current")
        Boolean isCurrent,

        @JsonProperty("note")
        String note
) {

    @AssertTrue(
            message =
                    "End date cannot be earlier than start date"
    )
    public boolean isDateRangeValid() {
        return startDate == null
                || endDate == null
                || !endDate.isBefore(startDate);
    }

    @AssertTrue(
            message =
                    "Current affiliation cannot have an end date"
    )
    public boolean isCurrentStatusValid() {
        return !Boolean.TRUE.equals(isCurrent)
                || endDate == null;
    }
}