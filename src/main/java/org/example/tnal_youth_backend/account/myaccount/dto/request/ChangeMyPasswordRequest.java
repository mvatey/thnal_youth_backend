package org.example.tnal_youth_backend.account.myaccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMyPasswordRequest(

        @NotBlank(message = "New password is required")
        @Size(
                min = 6,
                message = "New password must contain at least 6 characters"
        )
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword

) {
}
