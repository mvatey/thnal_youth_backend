package org.example.tnal_youth_backend.account.myaccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMyPasswordRequest(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(
                min = 8,
                max = 100,
                message = "New password must contain between 8 and 100 characters"
        )
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword

) {
}