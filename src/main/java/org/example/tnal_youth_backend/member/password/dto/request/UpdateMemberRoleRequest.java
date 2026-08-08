package org.example.tnal_youth_backend.member.password.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

public record UpdateMemberRoleRequest(

        @NotNull(
                message = "Role is required"
        )
        UserRole role
) {
}