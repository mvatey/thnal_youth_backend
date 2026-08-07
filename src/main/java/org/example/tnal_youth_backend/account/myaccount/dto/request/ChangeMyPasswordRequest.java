package org.example.tnal_youth_backend.account.myaccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeMyPasswordRequest(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "\\d{6}",
                message = "New password must contain exactly 6 digits"
        )
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword

) {
}
