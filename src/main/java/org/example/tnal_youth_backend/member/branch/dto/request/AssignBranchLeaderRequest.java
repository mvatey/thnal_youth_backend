package org.example.tnal_youth_backend.member.branch.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AssignBranchLeaderRequest(

        @JsonProperty("member_id")
        @NotNull(message = "Branch leader member ID is required")
        Long memberId
) {
}