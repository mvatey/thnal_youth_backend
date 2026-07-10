package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberPositionDto {
    private String code;
    private String labelKh;
    private String labelEn;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
}

