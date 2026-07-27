package org.example.tnal_youth_backend.donation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Mirrors the {@code donations} table (V8 + V23 + V24).
 *
 * <p>Plain POJO written for MyBatis (same style as {@code NotificationModel}) —
 * this is NOT a JPA entity, so column names are mapped explicitly in
 * {@code DonationRepo}. Money is {@link BigDecimal} to preserve the NUMERIC
 * precision of the schema (amounts 14,2; exchange rate 14,4).
 *
 * <p>Invariants enforced by the schema (and pre-validated in the service so the
 * caller gets a clean error code instead of a generic constraint violation):
 * <ul>
 *   <li>{@code chk_donation_source} — exactly ONE of member_id / sponsor_id /
 *       donor_name identifies the donor.</li>
 *   <li>{@code chk_donation_amounts} — amount_khr &ge; 0, amount_usd &ge; 0,
 *       and at least one is &gt; 0.</li>
 *   <li>{@code chk_donation_exchange_rate} — rate is NULL or &gt; 0.</li>
 *   <li>{@code chk_donation_total} — total_amount_usd &ge; 0.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationModel {

    private Long id;

    /** Human-facing unique number, minted by the service from donation_no_seq (V23). */
    private String donationNo;

    private Short donationTypeId;

    // --- donor source: exactly one of the three is non-null ---
    private Long memberId;
    private Long sponsorId;
    private String donorName;

    /** Optional activity this donation is allocated to. */
    private Long activityId;

    private Long branchId;

    /** Optional logical period the donation covers (e.g. the month for a monthly pledge). */
    private LocalDate donationPeriod;

    // --- money ---
    private BigDecimal amountKhr;
    private BigDecimal amountUsd;
    private BigDecimal exchangeRateKhrPerUsd;
    /** USD-normalised total; computed by the service, never trusted from the client. */
    private BigDecimal totalAmountUsd;

    private Short paymentMethodId;

    private OffsetDateTime paidAt;

    private String paymentReference;
    private Long receiptFileId;

    /** Populated from the authenticated principal (users.id). Set once, on insert. */
    private Long recordedBy;

    private String note;

    /** Optional idempotency key (V23). Passed to SQL as text and cast to uuid. */
    private String clientRequestId;

    /**
     * Who last edited this donation (users.id), set by the service on every update
     * (V24). Never carried on insert — a freshly recorded donation has no editor.
     */
    private Long updatedBy;

    /**
     * OPTIMISTIC-LOCK GUARD (write-only, not a column). When non-null the update
     * SQL adds {@code AND updated_at = #{expectedUpdatedAt}} so a stale edit
     * affects 0 rows and the service can raise DONATION_UPDATE_CONFLICT instead of
     * silently clobbering a concurrent change. Omitting it keeps the previous
     * last-writer-wins behaviour.
     */
    private OffsetDateTime expectedUpdatedAt;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
