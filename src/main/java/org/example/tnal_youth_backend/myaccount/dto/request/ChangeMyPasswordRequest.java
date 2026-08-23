package org.example.tnal_youth_backend.myaccount.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMyPasswordRequest(

        @JsonProperty("new_password")
        @NotBlank(
                message =
                        "New password is required"
        )
        @Size(
                min = 6,
                message =
                        "New password must contain at least 6 characters"
        )
        String newPassword,

        @JsonProperty("confirm_password")
        @NotBlank(
                message =
                        "Password confirmation is required"
        )
        String confirmPassword
) {
}
