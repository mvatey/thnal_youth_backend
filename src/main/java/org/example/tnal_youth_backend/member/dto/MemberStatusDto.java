package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberStatusDto {
    private Long id;
    private String code;
    private String labelKm;
    private String labelEn;
    private boolean isActive;
    private int sortOrder;
}
