package org.example.tnal_youth_backend.myaccount.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMyEmailRequest(

        @JsonProperty("new_email")
        @NotBlank(message = "New email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String newEmail
) {
}
