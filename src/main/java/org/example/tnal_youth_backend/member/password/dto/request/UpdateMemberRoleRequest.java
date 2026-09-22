package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

public record UpdateMemberRoleRequest(

        @NotNull(
                message = "Role is required"
        )
        UserRole role,

        // Only meaningful when role is BRANCH_LEADER -- which specific
        // leader-mapped position (e.g. a "deputy" one distinct from the
        // canonical "ប្រធានសាខា") this promotion should be recorded
        // under. Omitted falls back to the canonical BRANCH_LEADER-coded
        // position.
        @JsonProperty("position_id")
        Short positionId
) {
}
