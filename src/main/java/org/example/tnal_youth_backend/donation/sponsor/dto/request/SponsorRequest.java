package org.example.tnal_youth_backend.donation.sponsor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SponsorRequest(

        @NotNull(
                message = "Sponsor type ID is required"
        )
        Short sponsorTypeId,

        @NotBlank(
                message = "Sponsor name is required"
        )
        @Size(
                max = 255,
                message = "Sponsor name must not exceed 255 characters"
        )
        String name,

        @Size(
                max = 30,
                message = "Phone number must not exceed 30 characters"
        )
        String phone,

        @Email(
                message = "Email format is invalid"
        )
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @Size(
                max = 5000,
                message = "Address must not exceed 5000 characters"
        )
        String address,

        @Size(
                max = 5000,
                message = "Note must not exceed 5000 characters"
        )
        String note,

        @NotNull(
                message = "Active status is required"
        )
        Boolean isActive
) {
}