package org.example.tnal_youth_backend.activity.expense.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.exchangerate.entity.ExchangeRate;
import org.example.tnal_youth_backend.file.entity.FileEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_expenses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "activity_id",
            nullable = false
    )
    private Activity activity;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    /*
     * Quantity is descriptive only.
     * It is not multiplied by amountKHR or amountUSD.
     */
    @Column(
            name = "quantity",
            nullable = false,
            precision = 10,
            scale = 2
    )
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    /*
     * Actual amount spent in KHR for this row.
     */
    @Column(
            name = "amount_khr",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal amountKhr = BigDecimal.ZERO;

    /*
     * Actual amount spent in USD for this row.
     */
    @Column(
            name = "amount_usd",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal amountUsd = BigDecimal.ZERO;

    /*
     * The exact historical exchange-rate record used.
     */
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "exchange_rate_id")
    private ExchangeRate exchangeRate;

    /*
     * Snapshot of the exchange-rate value.
     *
     * Example:
     * 1 USD = 4,100 KHR
     *
     * Saved so old expenses never change when the
     * current exchange rate changes later.
     */
    @Column(
            name = "exchange_rate_value",
            precision = 18,
            scale = 6
    )
    private BigDecimal exchangeRateValue;

    /*
     * amountKHR divided by exchangeRateValue.
     */
    @Column(
            name = "converted_khr_to_usd",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal convertedKhrToUsd =
            BigDecimal.ZERO;

    /*
     * amountUSD + convertedKhrToUsd.
     */
    @Column(
            name = "total_amount_usd",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal totalAmountUsd =
            BigDecimal.ZERO;

    @Column(name = "spent_on")
    private LocalDate spentOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_file_id")
    private FileEntity receiptFile;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "recorded_by",
            nullable = false
    )
    private User recordedBy;

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

        if (quantity == null) {
            quantity = BigDecimal.ONE;
        }

        if (amountKhr == null) {
            amountKhr = BigDecimal.ZERO;
        }

        if (amountUsd == null) {
            amountUsd = BigDecimal.ZERO;
        }

        if (convertedKhrToUsd == null) {
            convertedKhrToUsd = BigDecimal.ZERO;
        }

        if (totalAmountUsd == null) {
            totalAmountUsd = BigDecimal.ZERO;
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
        updatedAt = OffsetDateTime.now();
    }
}