package org.example.tnal_youth_backend.myaccount.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;

import java.time.LocalDate;

public record UpdateMyPersonalInfoRequest(

        @JsonProperty("full_name_km")
        @NotBlank(
                message = "Khmer full name is required"
        )
        @Size(
                max = 255,
                message = "Khmer full name must not exceed 255 characters"
        )
        String fullNameKm,

        @JsonProperty("full_name_en")
        @Size(
                max = 255,
                message = "English full name must not exceed 255 characters"
        )
        String fullNameEn,

        Gender gender,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        @Email(
                message = "Email format is invalid"
        )
        String email,

        @Size(
                max = 30,
                message = "Phone must not exceed 30 characters"
        )
        String phone,

        @JsonProperty("religion_id")
        Short religionId,

        @JsonProperty("ethnicity_id")
        Short ethnicityId,

        @JsonProperty("nationality_id")
        Short nationalityId,

        @JsonProperty("member_level_id")
        Short memberLevelId,

        @JsonProperty("tshirt_size")
        TshirtSize tshirtSize,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress
) {
}