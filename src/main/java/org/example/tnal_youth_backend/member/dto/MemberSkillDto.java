package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberSkillDto {
    private Long memberId;

    // Keep only skill-related fields
    private String skill;
    private String culturalLevel;

    // ⚠ Language proficiency should stay in MemberLanguageDto
    // Old fields kept for now to avoid breaking code, but mark them deprecated
    @Deprecated private String languageEn;
    @Deprecated private String languageKh;
    @Deprecated private String listeningLevel;
    @Deprecated private String readingLevel;
    @Deprecated private String speakingLevel;
    @Deprecated private String writingLevel;
}

