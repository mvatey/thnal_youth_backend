package org.example.tnal_youth_backend.account.memberfamily.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.family.entity.FamilyLifeStatus;
import org.example.tnal_youth_backend.member.family.entity.FamilyRelationship;

import java.time.LocalDate;

public record MyFamilyRequest(

        @NotNull(
                message = "Family relationship is required"
        )
        FamilyRelationship relationship,

        @NotBlank(
                message = "Khmer full name is required"
        )
        @Size(
                max = 255,
                message = "Khmer full name must not exceed 255 characters"
        )
        String fullNameKm,

        @Size(
                max = 255,
                message = "English full name must not exceed 255 characters"
        )
        String fullNameEn,

        @PastOrPresent(
                message = "Date of birth cannot be in the future"
        )
        LocalDate dateOfBirth,

        @Size(
                max = 255,
                message = "Occupation must not exceed 255 characters"
        )
        String occupation,

        FamilyLifeStatus lifeStatus,

        @Size(
                max = 255,
                message = "Address must not exceed 255 characters"
        )
        String address
) {
}