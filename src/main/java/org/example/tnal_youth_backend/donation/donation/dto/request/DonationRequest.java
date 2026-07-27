package org.example.tnal_youth_backend.donation.donation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DonationRequest(

        @NotNull(message = "Donation type ID is required")
        Short donationTypeId,

        Long memberId,

        Long sponsorId,

        @Size(
                max = 255,
                message = "Donor name must not exceed 255 characters"
        )
        String donorName,

        Long activityId,

        @NotNull(message = "Branch ID is required")
        Long branchId,

        LocalDate donationPeriod,

        @NotNull(message = "KHR amount is required")
        @DecimalMin(
                value = "0.00",
                message = "KHR amount cannot be negative"
        )
        @Digits(
                integer = 16,
                fraction = 2,
                message = "KHR amount is invalid"
        )
        BigDecimal amountKhr,

        @NotNull(message = "USD amount is required")
        @DecimalMin(
                value = "0.00",
                message = "USD amount cannot be negative"
        )
        @Digits(
                integer = 16,
                fraction = 2,
                message = "USD amount is invalid"
        )
        BigDecimal amountUsd,

        @DecimalMin(
                value = "0.01",
                message = "Exchange rate must be greater than zero"
        )
        @Digits(
                integer = 12,
                fraction = 6,
                message = "Exchange rate is invalid"
        )
        BigDecimal exchangeRateKhrPerUsd,

        @NotNull(message = "Payment method ID is required")
        Short paymentMethodId,

        @NotNull(message = "Paid date and time are required")
        OffsetDateTime paidAt,

        @Size(
                max = 255,
                message = "Payment reference must not exceed 255 characters"
        )
        String paymentReference,

        Long receiptFileId,

        @Size(
                max = 5000,
                message = "Note must not exceed 5000 characters"
        )
        String note
) {
}