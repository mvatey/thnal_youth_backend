package org.example.tnal_youth_backend.member.language.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record MemberLanguageResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("language_name")
        String languageName,

        @JsonProperty("listening_level_id")
        Short listeningLevelId,

        @JsonProperty("speaking_level_id")
        Short speakingLevelId,

        @JsonProperty("reading_level_id")
        Short readingLevelId,

        @JsonProperty("writing_level_id")
        Short writingLevelId,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}