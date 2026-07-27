package org.example.tnal_youth_backend.member.education.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberEducationRequest(

        @JsonProperty("school_name")
        @NotBlank(message = "School name is required")
        @Size(
                max = 255,
                message = "School name must not exceed 255 characters"
        )
        String schoolName,

        @JsonProperty("education_level_id")
        @NotNull(message = "Education level ID is required")
        Short educationLevelId,

        @JsonProperty("field_of_study")
        @Size(
                max = 255,
                message = "Field of study must not exceed 255 characters"
        )
        String fieldOfStudy,

        @JsonProperty("country_code")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "Country code must contain exactly two letters"
        )
        String countryCode,

        @JsonProperty("country_name")
        @Size(
                max = 100,
                message = "Country name must not exceed 100 characters"
        )
        String countryName,

        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("province_name")
        @Size(
                max = 100,
                message = "Province name must not exceed 100 characters"
        )
        String provinceName,

        @JsonProperty("certificate_file_id")
        Long certificateFileId,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
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

    @AssertTrue(
            message = "Cambodian education must use province_id, not province_name"
    )
    public boolean isCambodianLocationValid() {
        if (countryCode == null) {
            return true;
        }

        if ("KH".equalsIgnoreCase(countryCode)) {
            return provinceName == null
                    || provinceName.isBlank();
        }

        return provinceId == null;
    }
}