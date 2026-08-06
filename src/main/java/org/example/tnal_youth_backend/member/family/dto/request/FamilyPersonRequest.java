package org.example.tnal_youth_backend.member.family.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.family.entity.FamilyLifeStatus;

import java.time.LocalDate;

public record FamilyPersonRequest(

        @JsonProperty("full_name_km")
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

        @Size(
                max = 255,
                message = "Occupation must not exceed 255 characters"
        )
        String occupation,

        @JsonProperty("life_status")
        FamilyLifeStatus lifeStatus,

        @Size(
                max = 255,
                message = "Address must not exceed 255 characters"
        )
        String address
) {
}