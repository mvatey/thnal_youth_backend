package org.example.tnal_youth_backend.account.memberdonation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MyDonationResponse(

        /*
         * Donation
         */
        Long donationId,

        String donationNo,

        LocalDate donationPeriod,

        BigDecimal amountKhr,

        BigDecimal amountUsd,

        BigDecimal exchangeRateKhrPerUsd,

        BigDecimal totalAmountUsd,

        OffsetDateTime paidAt,

        String paymentReference,

        String note,

        /*
         * Donation type
         */
        Long donationTypeId,

        String donationTypeCode,

        String donationTypeLabelKm,

        String donationTypeLabelEn,

        /*
         * Payment method
         */
        Long paymentMethodId,

        String paymentMethodCode,

        String paymentMethodLabelKm,

        String paymentMethodLabelEn,

        /*
         * Member
         */
        Long memberId,

        String memberNo,

        String memberFullNameKm,

        String memberFullNameEn,

        /*
         * Branch
         */
        Long branchId,

        String branchNameKm,

        String branchNameEn,

        /*
         * Related activity, when this is an activity donation
         */
        Long activityId,

        String activityTitleKm,

        String activityTitleEn,

        /*
         * Receipt
         */
        Long receiptFileId,

        String receiptFilePath,

        String receiptOriginalName,

        String receiptMimeType,

        Long receiptSizeBytes,

        /*
         * Audit information
         */
        Long recordedByUserId,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}