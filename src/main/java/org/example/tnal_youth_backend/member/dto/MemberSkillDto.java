package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberSkillDto {
    private Long memberId;
    private String languageEn;
    private String languageKh;
    private String listeningLevel;
    private String readingLevel;
    private String speakingLevel;
    private String writingLevel;
    private String skill;
    private String culturalLevel;
}
