package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberStatusDto {
    private String code;
    private String labelKh;
    private String labelEn;
    private Boolean isActive;
    private Integer sortOrder; // ✅ include this
}

