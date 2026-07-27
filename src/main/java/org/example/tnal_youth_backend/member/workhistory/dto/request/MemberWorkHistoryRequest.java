package org.example.tnal_youth_backend.member.workhistory.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberWorkHistoryRequest(

        @JsonProperty("organization_name")
        @NotBlank(message = "Organization name is required")
        @Size(
                max = 255,
                message = "Organization name must not exceed 255 characters"
        )
        String organizationName,

        @JsonProperty("position_title")
        @NotBlank(message = "Position title is required")
        @Size(
                max = 255,
                message = "Position title must not exceed 255 characters"
        )
        String positionTitle,

        @JsonProperty("address")
        @Size(
                max = 255,
                message = "Address must not exceed 255 characters"
        )
        String address,

        @JsonProperty("employment_sector_id")
        Short employmentSectorId,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate
) {

    @AssertTrue(
            message = "End date must be equal to or after start date"
    )
    public boolean isDateRangeValid() {
        return endDate == null
                || startDate == null
                || !endDate.isBefore(startDate);
    }
}