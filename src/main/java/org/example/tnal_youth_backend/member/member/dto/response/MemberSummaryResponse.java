package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MemberSummaryResponse(

        @JsonProperty("total_members")
        long totalMembers,

        @JsonProperty("total_members_change_percent")
        BigDecimal totalMembersChangePercent,

        @JsonProperty("female_members")
        long femaleMembers,

        @JsonProperty("female_members_change_percent")
        BigDecimal femaleMembersChangePercent,

        @JsonProperty("monk_members")
        long monkMembers,

        @JsonProperty("monk_members_change_percent")
        BigDecimal monkMembersChangePercent,

        @JsonProperty("buddhist_members")
        long buddhistMembers,

        @JsonProperty("buddhist_members_change_percent")
        BigDecimal buddhistMembersChangePercent,

        @JsonProperty("islam_members")
        long islamMembers,

        @JsonProperty("islam_members_change_percent")
        BigDecimal islamMembersChangePercent
) {
}