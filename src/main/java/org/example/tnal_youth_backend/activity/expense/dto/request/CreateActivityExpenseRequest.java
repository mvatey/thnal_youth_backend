package org.example.tnal_youth_backend.activity.expense.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateActivityExpenseRequest {

    @NotBlank(message = "Expense name is required")
    @Size(
            max = 255,
            message = "Expense name must not exceed 255 characters"
    )
    private String name;

    @Size(
            max = 2000,
            message = "Description must not exceed 2000 characters"
    )
    private String description;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.01",
            message = "Quantity must be greater than zero"
    )
    private BigDecimal quantity;

    @NotNull(message = "KHR amount is required")
    @DecimalMin(
            value = "0.00",
            message = "KHR amount cannot be negative"
    )
    @JsonProperty("amount_khr")
    private BigDecimal amountKhr;

    @NotNull(message = "USD amount is required")
    @DecimalMin(
            value = "0.00",
            message = "USD amount cannot be negative"
    )
    @JsonProperty("amount_usd")
    private BigDecimal amountUsd;

    @NotNull(message = "Spent date is required")
    @PastOrPresent(
            message = "Spent date cannot be in the future"
    )
    @JsonProperty("spent_on")
    private LocalDate spentOn;

    @JsonProperty("receipt_file_id")
    private Long receiptFileId;
}