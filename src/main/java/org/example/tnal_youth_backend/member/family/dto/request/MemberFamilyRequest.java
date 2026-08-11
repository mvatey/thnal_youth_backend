package org.example.tnal_youth_backend.member.family.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.family.entity.FamilyLifeStatus;
import org.example.tnal_youth_backend.member.family.entity.FamilyRelationship;

import java.time.LocalDate;

public record MemberFamilyRequest(

        @JsonProperty("relationship")
        @NotNull(message = "Family relationship is required")
        FamilyRelationship relationship,

        @JsonProperty("full_name_km")
        @NotBlank(message = "Khmer full name is required")
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

        @JsonProperty("date_of_birth")
        @PastOrPresent(
                message = "Date of birth cannot be in the future"
        )
        LocalDate dateOfBirth,

        @JsonProperty("occupation")
        @Size(
                max = 255,
                message = "Occupation must not exceed 255 characters"
        )
        String occupation,

        @JsonProperty("life_status")
        FamilyLifeStatus lifeStatus,

        @JsonProperty("address")
        @Size(
                max = 255,
                message = "Address must not exceed 255 characters"
        )
        String address
) {
}