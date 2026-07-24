package org.example.tnal_youth_backend.member.politicalaffiliation.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberPoliticalAffiliationRequest(

        @JsonAlias("affiliation_name")
        @NotBlank(message = "Affiliation name is required")
        @Size(
                max = 255,
                message = "Affiliation name must not exceed 255 characters"
        )
        String affiliationName,

        @JsonAlias("position_title")
        @Size(
                max = 255,
                message = "Position title must not exceed 255 characters"
        )
        String positionTitle,

        @Size(
                max = 255,
                message = "Location must not exceed 255 characters"
        )
        String location,

        @JsonAlias("start_date")
        LocalDate startDate,

        @JsonAlias("end_date")
        LocalDate endDate
) {

    @AssertTrue(
            message = "End date must be equal to or after start date"
    )
    public boolean isDateRangeValid() {
        return startDate == null
                || endDate == null
                || !endDate.isBefore(startDate);
    }
}