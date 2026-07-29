package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberSummaryResponse(

        @JsonProperty("total_members")
        long totalMembers,

        @JsonProperty("female_members")
        long femaleMembers,

        @JsonProperty("monk_members")
        long monkMembers,

        @JsonProperty("buddhist_members")
        long buddhistMembers,

        @JsonProperty("islam_members")
        long islamMembers
) {
}