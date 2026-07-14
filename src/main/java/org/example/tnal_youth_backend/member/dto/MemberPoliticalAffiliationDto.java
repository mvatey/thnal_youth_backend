package org.example.tnal_youth_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberPoliticalAffiliationDto {
    private Long memberId;
    private String partyNameEn;
    private String partyNameKh;
    private String roleEn;
    private String roleKh;
    private String countryEn;
    private String countryKh;
    private String appointmentCode;
    private String workplaceEn;
    private String workplaceKh;
    private LocalDate startDate;
    private LocalDate endDate;

    // NEW: active/inactive status
    private String status;
}

