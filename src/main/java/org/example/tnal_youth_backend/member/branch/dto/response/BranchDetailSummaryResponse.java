package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BranchDetailSummaryResponse(

        @JsonProperty("total_members")
        long totalMembers,

        @JsonProperty("total_activities")
        long totalActivities
) {
}