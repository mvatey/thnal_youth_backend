package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyActivationOtpRequest(

        @NotBlank(
                message = "Phone number or email is required"
        )
        String phoneOrEmail,

        @NotBlank(
                message = "OTP is required"
        )
        String otp
) {
}