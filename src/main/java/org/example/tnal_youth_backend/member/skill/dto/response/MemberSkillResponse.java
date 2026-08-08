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

        @JsonProperty("certificate_file")
        FileResponse certificateFile,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {

        public record FileResponse(

                Long id,

                @JsonProperty("file_path")
                String filePath,

                @JsonProperty("original_name")
                String originalName,

                @JsonProperty("mime_type")
                String mimeType,

                @JsonProperty("size_bytes")
                Long sizeBytes
        ) {
        }
}