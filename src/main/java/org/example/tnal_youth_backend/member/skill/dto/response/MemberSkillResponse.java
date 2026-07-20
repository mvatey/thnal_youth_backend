package org.example.tnal_youth_backend.member.skill.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record MemberSkillResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("skill_name")
        String skillName,

        @JsonProperty("proficiency_level_id")
        Short proficiencyLevelId,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}