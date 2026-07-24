package org.example.tnal_youth_backend.account.memberdonation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyDonationResponse(

        Long id,

        String donationNo,

        Short donationTypeId,

        Long activityId,

        Long branchId,

        LocalDate donationPeriod,

        BigDecimal amountKhr,

        BigDecimal amountUsd,

        BigDecimal totalAmountUsd,

        Short paymentMethodId,

        OffsetDateTime paidAt,

        String paymentReference,

        Long receiptFileId,

        String note
) {
}