package org.example.tnal_youth_backend.account.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record UserResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        String phone,

        String email,

        @JsonProperty("role_id")
        Short roleId,

        @JsonProperty("account_status_id")
        Short accountStatusId,

        @JsonProperty("last_login_at")
        OffsetDateTime lastLoginAt,

        @JsonProperty("failed_login_count")
        Integer failedLoginCount,

        @JsonProperty("locked_until")
        OffsetDateTime lockedUntil,

        @JsonProperty("created_by")
        Long createdById,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}