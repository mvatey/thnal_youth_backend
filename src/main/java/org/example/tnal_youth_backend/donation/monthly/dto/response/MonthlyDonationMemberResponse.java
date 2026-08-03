package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MonthlyDonationMemberResponse {
    private Long memberId;
    private String memberNo;
    private String fullNameKm;
    private String fullNameEn;
    private Long profilePhotoId;
    private String gender;
    private LocalDate dateOfBirth;
    private Long branchId;
    private String branchNameKm;
    private Long existingDonationId;
    private BigDecimal amountKhr;
    private BigDecimal amountUsd;
    private Short paymentMethodId;
    private String paymentMethodCode;
    private Long receiptFileId;
    private Boolean alreadyPaid;
}
