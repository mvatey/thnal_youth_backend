package org.example.tnal_youth_backend.member.branch.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignBranchLeaderRequest(
        @NotNull(message = "Member ID is required") Long memberId
) {}
