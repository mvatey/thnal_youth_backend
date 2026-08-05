package org.example.tnal_youth_backend.donation.sponsorflow.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SponsorDonationRowResponse {

    private Long donationId;

    private String donationNo;

    private String donorKind;

    private Long sponsorId;

    private Long memberId;

    private String name;

    private String phone;

    private String email;

    private String address;

    private Long branchId;

    private String branchNameKm;

    private Long activityId;

    private String activityTitleKm;

    private OffsetDateTime paidAt;

    private BigDecimal amountKhr;

    private BigDecimal amountUsd;

    private BigDecimal totalAmountUsd;

    private Short paymentMethodId;

    private String paymentMethodCode;

    private String paymentMethodLabelKm;

    private String paymentReference;

    private Long receiptFileId;

    private String materialCategory;

    private BigDecimal materialQuantity;

    private String materialQuantityType;

    private String purpose;

    private String note;

    private OffsetDateTime updatedAt;
}