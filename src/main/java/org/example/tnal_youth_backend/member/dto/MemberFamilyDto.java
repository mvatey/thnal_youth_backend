package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

@Data
public class MemberFamilyDto {
    private Long memberId;
    private String relation;
    private String fullNameEn;
    private String fullNameKh;
    private String phone;
    private String email;
    private String occupation;
}
