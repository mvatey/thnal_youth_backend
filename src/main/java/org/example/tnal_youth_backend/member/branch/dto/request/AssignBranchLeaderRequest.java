package org.example.tnal_youth_backend.member.branch.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AssignBranchLeaderRequest(

        @JsonProperty("member_id")
        @NotNull(message = "Branch leader member ID is required")
        Long memberId,

        // Which leader-mapped position (e.g. a "deputy" one distinct from
        // the canonical "ប្រធានសាខា") to record this under -- omitted
        // falls back to the canonical BRANCH_LEADER-coded position.
        @JsonProperty("position_id")
        Short positionId
) {
}