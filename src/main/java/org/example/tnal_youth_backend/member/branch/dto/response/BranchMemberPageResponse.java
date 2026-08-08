package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BranchMemberPageResponse(

        List<BranchMemberTableItemResponse> content,

        int page,

        int size,

        @JsonProperty("total_elements")
        long totalElements,

        @JsonProperty("total_pages")
        int totalPages,

        boolean first,

        boolean last
) {
}