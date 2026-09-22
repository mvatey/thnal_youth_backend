package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

public record UpdateMemberRoleRequest(

        @NotNull(
                message = "Role is required"
        )
        UserRole role,

        /*
         * Only read when role is BRANCH_LEADER and the target branch
         * already has an active leader -- without this set, that case is
         * rejected with a 409 naming the existing leader instead of
         * silently replacing them (same confirm/replace pattern as
         * creating a member straight into a leader position, see
         * CreateMemberRequest).
         */
        @JsonProperty("confirm_replace_leader")
        Boolean confirmReplaceLeader
) {
}