package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BranchDetailSummaryResponse(

        @JsonProperty("total_members")
        long totalMembers,

        @JsonProperty("total_activities")
        long totalActivities,

        @JsonProperty("total_donations_usd")
        BigDecimal totalDonationsUsd
) {
}
