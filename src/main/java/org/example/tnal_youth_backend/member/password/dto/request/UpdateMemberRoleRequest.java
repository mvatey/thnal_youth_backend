package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.ViewerScope;

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
        Short positionId,

        // Only meaningful when role is VIEWER -- what level this
        // member-linked viewer sees their own branch at. ADMIN scope is
        // deliberately never offered here (see validateRoleChange):
        // this is always a real member with a real branch, not an
        // organization-wide viewer.
        @JsonProperty("viewer_scope")
        ViewerScope viewerScope
) {
}
