package org.example.tnal_youth_backend.activity.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeMemberRowResponse {
    private Long donationId;
    private String donationNo;
    private Long memberId;
    private String memberNo;
    private String memberNameKm;
    private String memberNameEn;
    private Long profilePhotoId;
    private String gender;
    private LocalDate dateOfBirth;
    private BigDecimal amountKhr;
    private BigDecimal amountUsd;
    private BigDecimal exchangeRateKhrPerUsd;
    private BigDecimal totalAmountUsd;
    private Short paymentMethodId;
    private String paymentMethodCode;
    private String paymentMethodLabelKm;
    private String paymentMethodLabelEn;
    private String paymentReference;
    private Long receiptFileId;
    private String description;
    private OffsetDateTime receivedAt;
}
