package org.example.tnal_youth_backend.account.myaccount.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record UpdateMyAccountRequest(

        @NotBlank(message = "Phone number is required")
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

        @NotBlank(message = "Khmer full name is required")
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

        Gender gender,

        LocalDate dateOfBirth,

        String placeOfBirth,

        String currentAddress,

        String permanentAddress,

        String bio,

        /*
         * Existing row from the files table.
         * Send null when the profile photo is not being changed.
         */
        Long profilePhotoId

) {
}