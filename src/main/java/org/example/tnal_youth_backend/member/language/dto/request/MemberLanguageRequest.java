package org.example.tnal_youth_backend.member.language.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberLanguageRequest(

        @JsonProperty("language_name")
        @NotBlank(message = "Language name is required")
        @Size(
                max = 100,
                message = "Language name must not exceed 100 characters"
        )
        String languageName,

        @JsonProperty("listening_level_id")
        Short listeningLevelId,

        @JsonProperty("speaking_level_id")
        Short speakingLevelId,

        @JsonProperty("reading_level_id")
        Short readingLevelId,

        @JsonProperty("writing_level_id")
        Short writingLevelId
) {

        @AssertTrue(
                message = "At least one language proficiency level is required"
        )
        public boolean isAtLeastOneLevelProvided() {
                return listeningLevelId != null
                        || speakingLevelId != null
                        || readingLevelId != null
                        || writingLevelId != null;
        }
}