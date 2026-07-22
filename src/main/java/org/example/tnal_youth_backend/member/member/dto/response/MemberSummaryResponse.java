package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberSummaryResponse(

        @JsonProperty("total_members")
        long totalMembers,

        @JsonProperty("inactive_members")
        long inactiveMembers,

        @JsonProperty("leaders")
        long leaders
) {
}