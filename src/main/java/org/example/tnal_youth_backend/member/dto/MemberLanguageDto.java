package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberLanguageDto {
    private Long memberId;
    private String languageEn;
    private String languageKh;
    private String proficiencyLevel;
}
