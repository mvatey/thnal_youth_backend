package org.example.tnal_youth_backend.donation.monthly.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyDonationUpdateRequest {

    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)
    @JsonProperty("amount_khr")
    private BigDecimal amountKhr;

    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)
    @JsonProperty("amount_usd")
    private BigDecimal amountUsd;

    @NotNull
    @JsonProperty("payment_method_id")
    private Short paymentMethodId;

    @JsonProperty("receipt_file_id")
    private Long receiptFileId;
}
