package org.example.tnal_youth_backend.member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberWorkHistoryDto {
    private Long memberId;
    private String organization;
    private String positionTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
