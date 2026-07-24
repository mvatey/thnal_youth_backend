package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberSummaryResponse(

        @JsonProperty("male_members")
        long maleMembers,

        @JsonProperty("female_members")
        long femaleMembers,

        /*
         * The frontend can display this card as "Monk".
         */
        @JsonProperty("monk_members")
        long monkMembers,

        @JsonProperty("buddhist_members")
        long buddhistMembers,

        @JsonProperty("islam_members")
        long islamMembers
) {
}