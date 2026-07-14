package org.example.tnal_youth_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberTrainingDto {
    private Long memberId;
    private String institutionEn;
    private String institutionKh;
    private String provinceEn;
    private String provinceKh;
    private String countryEn;
    private String countryKh;
    private String degreeEn;
    private String degreeKh;
    private String link;
    private LocalDate startDate;
    private LocalDate endDate;
    private String descriptionEn;
    private String descriptionKh;

    // NEW: major/field of study
    private String majorEn;
    private String majorKh;
}
