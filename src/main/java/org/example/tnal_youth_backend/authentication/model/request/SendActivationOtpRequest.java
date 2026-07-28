package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;

public record SendActivationOtpRequest(

        @NotBlank(
                message = "Phone number or email is required"
        )
        String phoneOrEmail
) {
}