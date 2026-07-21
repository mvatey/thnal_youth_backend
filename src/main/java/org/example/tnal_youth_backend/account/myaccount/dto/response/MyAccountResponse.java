package org.example.tnal_youth_backend.account.myaccount.dto.response;

import java.time.OffsetDateTime;

public record MyAccountResponse(

        Long id,

        Long memberId,

        String phone,

        String email,

        Long roleId,

        Long accountStatusId,

        OffsetDateTime lastLoginAt,

        Integer failedLoginCount,

        OffsetDateTime lockedUntil,

        Long createdBy,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}