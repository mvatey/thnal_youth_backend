package org.example.tnal_youth_backend.donation.donation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "donations",
        indexes = {
                @Index(
                        name = "idx_donations_donation_type_id",
                        columnList = "donation_type_id"
                ),
                @Index(
                        name = "idx_donations_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_donations_activity_id",
                        columnList = "activity_id"
                ),
                @Index(
                        name = "idx_donations_branch_id",
                        columnList = "branch_id"
                ),
                @Index(
                        name = "idx_donations_payment_method_id",
                        columnList = "payment_method_id"
                ),
                @Index(
                        name = "idx_donations_paid_at",
                        columnList = "paid_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donation {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE =
            new BigDecimal("4000");

    private static final BigDecimal ZERO_AMOUNT =
            new BigDecimal("0.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /*
     * Human-readable donation number.
     *
     * Example:
     * DON-20260724-0001
     */
    @Column(
            name = "donation_no",
            nullable = false,
            unique = true,
            length = 100
    )
    private String donationNo;

    /*
     * donation_type_id -> donation_types.id
     */
    @Column(
            name = "donation_type_id",
            nullable = false
    )
    private Short donationTypeId;

    /*
     * member_id -> members.id
     *
     * Nullable because a donation can come from:
     * - a registered member
     * - a sponsor
     * - an external donor
     */
    @Column(name = "member_id")
    private Long memberId;

    /*
     * sponsor_id -> sponsors.id
     *
     * This remains an ID so the Donation module does not
     * require a Sponsor entity relationship.
     */
    @Column(name = "sponsor_id")
    private Long sponsorId;

    /*
     * Used when the donor is not a registered member
     * or registered sponsor.
     */
    @Column(
            name = "donor_name",
            length = 255
    )
    private String donorName;

    /*
     * activity_id -> activities.id
     *
     * Required only for donations connected to an activity.
     */
    @Column(name = "activity_id")
    private Long activityId;

    /*
     * branch_id -> branches.id
     */
    @Column(
            name = "branch_id",
            nullable = false
    )
    private Long branchId;

    /*
     * Used mainly for monthly donations.
     *
     * Example:
     * 2026-07-01 represents the July 2026 donation period.
     */
    @Column(name = "donation_period")
    private LocalDate donationPeriod;

    @Column(
            name = "amount_khr",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal amountKhr;

    @Column(
            name = "amount_usd",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal amountUsd;

    /*
     * Exchange rate used to convert KHR into USD.
     *
     * Example:
     * 4000 KHR = 1 USD
     */
    @Column(
            name = "exchange_rate_khr_per_usd",
            precision = 18,
            scale = 6
    )
    private BigDecimal exchangeRateKhrPerUsd;

    /*
     * Calculated as:
     *
     * amountUsd + (amountKhr / exchangeRateKhrPerUsd)
     */
    @Column(
            name = "total_amount_usd",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal totalAmountUsd;

    /*
     * payment_method_id -> payment_methods.id
     */
    @Column(
            name = "payment_method_id",
            nullable = false
    )
    private Short paymentMethodId;

    @Column(
            name = "paid_at",
            nullable = false
    )
    private OffsetDateTime paidAt;

    @Column(
            name = "payment_reference",
            length = 255
    )
    private String paymentReference;

    /*
     * receipt_file_id -> files.id
     */
    @Column(name = "receipt_file_id")
    private Long receiptFileId;

    /*
     * recorded_by -> users.id
     */
    @Column(
            name = "recorded_by",
            nullable = false
    )
    private Long recordedById;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
    private String note;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now =
                OffsetDateTime.now();

        normalizeAmounts();
        calculateTotalAmountUsd();

        if (paidAt == null) {
            paidAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeAmounts();
        calculateTotalAmountUsd();

        updatedAt =
                OffsetDateTime.now();
    }

    /*
     * Prevent null amount values.
     */
    private void normalizeAmounts() {
        if (amountKhr == null) {
            amountKhr = ZERO_AMOUNT;
        }

        if (amountUsd == null) {
            amountUsd = ZERO_AMOUNT;
        }

        if (exchangeRateKhrPerUsd == null
                || exchangeRateKhrPerUsd
                .compareTo(BigDecimal.ZERO) <= 0) {

            exchangeRateKhrPerUsd =
                    DEFAULT_EXCHANGE_RATE;
        }

        amountKhr =
                amountKhr.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        amountUsd =
                amountUsd.setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    /*
     * totalAmountUsd =
     * amountUsd + amountKhr / exchangeRateKhrPerUsd
     */
    public void calculateTotalAmountUsd() {
        BigDecimal safeAmountKhr =
                amountKhr == null
                        ? ZERO_AMOUNT
                        : amountKhr;

        BigDecimal safeAmountUsd =
                amountUsd == null
                        ? ZERO_AMOUNT
                        : amountUsd;

        BigDecimal safeExchangeRate =
                exchangeRateKhrPerUsd;

        if (safeExchangeRate == null
                || safeExchangeRate
                .compareTo(BigDecimal.ZERO) <= 0) {

            safeExchangeRate =
                    DEFAULT_EXCHANGE_RATE;
        }

        BigDecimal convertedKhrToUsd =
                safeAmountKhr.divide(
                        safeExchangeRate,
                        2,
                        RoundingMode.HALF_UP
                );

        totalAmountUsd =
                safeAmountUsd
                        .add(convertedKhrToUsd)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );
    }
}