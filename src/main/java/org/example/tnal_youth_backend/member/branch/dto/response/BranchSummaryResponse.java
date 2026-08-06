package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BranchSummaryResponse(

        @JsonProperty("total_branches")
        long totalBranches,

        @JsonProperty("active_branches")
        long activeBranches,

        @JsonProperty("total_members")
        long totalMembers
) {
}