package org.example.tnal_youth_backend.systemsettings.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RevealDefaultMemberPasswordRequest(

        @JsonProperty("current_password")
        @NotBlank(
                message =
                        "Your current password is required"
        )
        String currentPassword
) {
}
