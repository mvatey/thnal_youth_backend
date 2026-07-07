package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberPositionDto {
    private Long id;
    private String code;
    private String labelKm;
    private String labelEn;
    private String description;
    private boolean isActive;
    private int sortOrder;
}
