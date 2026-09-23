package org.example.tnal_youth_backend.member.password.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record MemberPasswordStatusResponse(

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("has_account")
        boolean hasAccount,

        @JsonProperty("is_activated")
        boolean activated,

        String phone,

        String email,

        String role,

        // Only meaningful when role is VIEWER -- which branch-scoped
        // level (BRANCH_LEADER or SECRETARY) this member-linked viewer
        // sees their own branch at.
        @JsonProperty("viewer_scope")
        String viewerScope,

        String status,

        @JsonProperty("activated_at")
        OffsetDateTime activatedAt,

        @JsonProperty("last_login_at")
        OffsetDateTime lastLoginAt
) {
}