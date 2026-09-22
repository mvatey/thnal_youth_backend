package org.example.tnal_youth_backend.systemsettings.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.tnal_youth_backend.common.validation.PasswordPolicy;

public record UpdateDefaultMemberPasswordRequest(

        @JsonProperty("current_password")
        @NotBlank(
                message =
                        "Your current password is required"
        )
        String currentPassword,

        @JsonProperty("new_default_password")
        @NotBlank(
                message =
                        "The new default password is required"
        )
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String newDefaultPassword
) {
}
