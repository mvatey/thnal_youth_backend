package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BranchDetailPageResponse(

        BranchDetailResponse branch,

        BranchDetailSummaryResponse summary,

        @JsonProperty("leaders")
        List<BranchLeaderResponse> leaders
) {
}