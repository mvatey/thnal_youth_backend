package org.example.tnal_youth_backend.account.myaccount.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMyAccountRequest(

        @Size(
                max = 30,
                message = "Phone number must not exceed 30 characters"
        )
        String phone,

        @Email(message = "Email format is invalid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

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

        @Size(
                max = 500,
                message = "Profile image path must not exceed 500 characters"
        )
        String profileImage

) {
}