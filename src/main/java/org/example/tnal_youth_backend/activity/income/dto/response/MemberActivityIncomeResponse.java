package org.example.tnal_youth_backend.activity.income.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MemberActivityIncomeResponse(
        Long donationId,
        String donationNo,
        Long activityId,
        String activityTitleKm,
        String activityTitleEn,
        BigDecimal amountKhr,
        BigDecimal amountUsd,
        BigDecimal totalAmountUsd,
        String paymentMethodCode,
        String paymentMethodLabelKm,
        String paymentMethodLabelEn,
        OffsetDateTime receivedAt,
        Long recordedById,
        String recordedByName,
        Long receiptFileId,
        String note
) {
}
