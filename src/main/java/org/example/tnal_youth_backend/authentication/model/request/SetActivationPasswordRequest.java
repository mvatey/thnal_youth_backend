package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetActivationPasswordRequest(

        @NotBlank(
                message = "Phone number or email is required"
        )
        String phoneOrEmail,

        @NotBlank(
                message = "OTP is required"
        )
        String otp,

        @NotBlank(
                message = "New password is required"
        )
        @Pattern(
                regexp = "\\d{6}",
                message = "Password must contain exactly 6 digits"
        )
        String newPassword
) {
}
