package org.example.tnal_youth_backend.donation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Full-replace update payload for correcting a recorded donation.
 *
 * <p>PUT semantics: every mutable field is (re)supplied and the same cross-field
 * rules as create are re-validated in the service. {@code donationNo},
 * {@code recordedBy}, {@code clientRequestId} and the timestamps are immutable via
 * the API and are never taken from this DTO.
 */
@Data
public class DonationUpdateDTO {

    @NotNull
    private Short donationTypeId;

    private Long memberId;
    private Long sponsorId;

    @Size(max = 255, message = "donorName must be 255 characters or fewer")
    private String donorName;

    private Long activityId;

    @NotNull
    private Long branchId;

    private LocalDate donationPeriod;

    @DecimalMin(value = "0.00", message = "amountKhr must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "amountKhr must fit NUMERIC(14,2)")
    private BigDecimal amountKhr;

    @DecimalMin(value = "0.00", message = "amountUsd must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "amountUsd must fit NUMERIC(14,2)")
    private BigDecimal amountUsd;

    @DecimalMin(value = "0.0", inclusive = false, message = "exchangeRateKhrPerUsd must be positive")
    @Digits(integer = 10, fraction = 4, message = "exchangeRateKhrPerUsd must fit NUMERIC(14,4)")
    private BigDecimal exchangeRateKhrPerUsd;

    @NotNull
    private Short paymentMethodId;

    @NotNull
    @PastOrPresent(message = "paidAt cannot be in the future")
    private OffsetDateTime paidAt;

    @Size(max = 100, message = "paymentReference must be 100 characters or fewer")
    private String paymentReference;

    private Long receiptFileId;

    @Size(max = 4000, message = "note must be 4000 characters or fewer")
    private String note;

    /**
     * OPTIONAL optimistic-lock guard. Send back the {@code updatedAt} you read on
     * the donation you are editing; the update then only succeeds if the row has
     * not changed since, otherwise the service returns
     * {@code DONATION_UPDATE_CONFLICT} (400) instead of silently overwriting a
     * concurrent edit. Omit it to keep the previous last-writer-wins behaviour
     * (backward compatible).
     */
    private OffsetDateTime expectedUpdatedAt;
}
