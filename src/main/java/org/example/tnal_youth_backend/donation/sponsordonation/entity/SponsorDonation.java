package org.example.tnal_youth_backend.donation.sponsordonation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "sponsor_donations",
        indexes = {
                @Index(
                        name = "idx_sponsor_donations_sponsor_id",
                        columnList = "sponsor_id"
                ),
                @Index(
                        name = "idx_sponsor_donations_branch_id",
                        columnList = "branch_id"
                ),
                @Index(
                        name = "idx_sponsor_donations_payment_method_id",
                        columnList = "payment_method_id"
                ),
                @Index(
                        name = "idx_sponsor_donations_paid_at",
                        columnList = "paid_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorDonation {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE =
            new BigDecimal("4000");

    private static final BigDecimal ZERO_AMOUNT =
            new BigDecimal("0.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "sponsor_donation_no",
            nullable = false,
            unique = true,
            length = 100
    )
    private String sponsorDonationNo;

    @Column(
            name = "sponsor_id",
            nullable = false
    )
    private Long sponsorId;

    @Column(
            name = "branch_id",
            nullable = false
    )
    private Long branchId;

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

    @Column(
            name = "exchange_rate_khr_per_usd",
            nullable = false,
            precision = 18,
            scale = 6
    )
    private BigDecimal exchangeRateKhrPerUsd;

    @Column(
            name = "total_amount_usd",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal totalAmountUsd;

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

    @Column(name = "receipt_file_id")
    private Long receiptFileId;

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
        OffsetDateTime now = OffsetDateTime.now();

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
        updatedAt = OffsetDateTime.now();
    }

    private void normalizeAmounts() {
        if (amountKhr == null) {
            amountKhr = ZERO_AMOUNT;
        }

        if (amountUsd == null) {
            amountUsd = ZERO_AMOUNT;
        }

        if (exchangeRateKhrPerUsd == null
                || exchangeRateKhrPerUsd.compareTo(BigDecimal.ZERO) <= 0) {
            exchangeRateKhrPerUsd = DEFAULT_EXCHANGE_RATE;
        }

        amountKhr = amountKhr.setScale(
                2,
                RoundingMode.HALF_UP
        );

        amountUsd = amountUsd.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    public void calculateTotalAmountUsd() {
        BigDecimal safeKhr =
                amountKhr == null ? ZERO_AMOUNT : amountKhr;

        BigDecimal safeUsd =
                amountUsd == null ? ZERO_AMOUNT : amountUsd;

        BigDecimal safeRate = exchangeRateKhrPerUsd;

        if (safeRate == null
                || safeRate.compareTo(BigDecimal.ZERO) <= 0) {
            safeRate = DEFAULT_EXCHANGE_RATE;
        }

        BigDecimal convertedKhrToUsd =
                safeKhr.divide(
                        safeRate,
                        2,
                        RoundingMode.HALF_UP
                );

        totalAmountUsd =
                safeUsd.add(convertedKhrToUsd)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );
    }
}
