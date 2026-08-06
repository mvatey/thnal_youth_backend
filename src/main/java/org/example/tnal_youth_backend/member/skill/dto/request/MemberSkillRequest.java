package org.example.tnal_youth_backend.member.skill.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberSkillRequest(

        @JsonProperty("skill_name")
        @NotBlank(
                message = "Skill name is required"
        )
        @Size(
                max = 150,
                message =
                        "Skill name must not exceed 150 characters"
        )
        String skillName,

        @JsonProperty("proficiency_level_id")
        @NotNull(
                message =
                        "Proficiency level is required"
        )
        Short proficiencyLevelId
) {
}