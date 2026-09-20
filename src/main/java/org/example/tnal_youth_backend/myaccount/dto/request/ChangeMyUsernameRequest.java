package org.example.tnal_youth_backend.myaccount.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMyUsernameRequest(

        @JsonProperty("new_username")
        @NotBlank(message = "New username is required")
        @Size(max = 255, message = "Username must not exceed 255 characters")
        String newUsername
) {
}
