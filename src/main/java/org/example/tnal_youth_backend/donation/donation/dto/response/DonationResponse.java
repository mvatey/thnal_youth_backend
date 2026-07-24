package org.example.tnal_youth_backend.donation.donation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DonationResponse(

        Long id,

        @JsonProperty("donation_no")
        String donationNo,

        @JsonProperty("donation_type_id")
        Short donationTypeId,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("sponsor_id")
        Long sponsorId,

        @JsonProperty("donor_name")
        String donorName,

        @JsonProperty("activity_id")
        Long activityId,

        @JsonProperty("branch_id")
        Long branchId,

        @JsonProperty("donation_period")
        LocalDate donationPeriod,

        @JsonProperty("amount_khr")
        BigDecimal amountKhr,

        @JsonProperty("amount_usd")
        BigDecimal amountUsd,

        @JsonProperty("exchange_rate_khr_per_usd")
        BigDecimal exchangeRateKhrPerUsd,

        @JsonProperty("total_amount_usd")
        BigDecimal totalAmountUsd,

        @JsonProperty("payment_method_id")
        Short paymentMethodId,

        @JsonProperty("paid_at")
        OffsetDateTime paidAt,

        @JsonProperty("payment_reference")
        String paymentReference,

        @JsonProperty("receipt_file_id")
        Long receiptFileId,

        String note
) {
}