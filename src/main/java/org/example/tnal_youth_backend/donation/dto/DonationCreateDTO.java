package org.example.tnal_youth_backend.donation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Create payload for recording a donation.
 *
 * <p>Field-level annotations cover shape/range only. Cross-field rules that the
 * DB models as CHECK constraints are validated in {@code DonationService} so the
 * caller gets a specific error code:
 * <ul>
 *   <li>exactly one donor source (memberId / sponsorId / donorName),</li>
 *   <li>at least one amount &gt; 0,</li>
 *   <li>exchange rate required when amountKhr &gt; 0 (so USD total is derivable),</li>
 *   <li>lookup ids exist / are active.</li>
 * </ul>
 * {@code totalAmountUsd} is intentionally NOT accepted from the client — it is
 * computed server-side.
 */
@Data
public class DonationCreateDTO {

    @NotNull
    private Short donationTypeId;

    // --- donor source: supply EXACTLY one of these three ---
    private Long memberId;
    private Long sponsorId;

    @Size(max = 255, message = "donorName must be 255 characters or fewer")
    private String donorName;

    /** Optional; required by the service when the type is ACTIVITY_DONATION. */
    private Long activityId;

    @NotNull
    private Long branchId;

    /** Optional; required by the service when the type is MONTHLY_DONATION. */
    private LocalDate donationPeriod;

    // --- money (14,2 / 14,4 to match the schema) ---
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
    private OffsetDateTime paidAt;

    @Size(max = 100, message = "paymentReference must be 100 characters or fewer")
    private String paymentReference;

    /** Optional id of a previously uploaded receipt in the files table. */
    private Long receiptFileId;

    @Size(max = 4000, message = "note must be 4000 characters or fewer")
    private String note;

    /**
     * Optional idempotency key (client-generated UUID). Two creates from the same
     * recorder with the same clientRequestId collapse to one donation — protecting
     * against double-submit / retries inserting twice. Omit it to keep the plain
     * (non-idempotent) behaviour.
     */
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "clientRequestId must be a UUID")
    private String clientRequestId;
}
