package org.example.tnal_youth_backend.activity.income.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityIncomeItemRequest {

    @NotNull(message = "member_id is required")
    @JsonProperty("member_id")
    private Long memberId;

    @DecimalMin(value = "0.00", message = "amount_khr must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "amount_khr must fit NUMERIC(14,2)")
    @JsonProperty("amount_khr")
    private BigDecimal amountKhr;

    @DecimalMin(value = "0.00", message = "amount_usd must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "amount_usd must fit NUMERIC(14,2)")
    @JsonProperty("amount_usd")
    private BigDecimal amountUsd;

    @NotNull(message = "payment_method_id is required")
    @JsonProperty("payment_method_id")
    private Short paymentMethodId;

    @Size(max = 100, message = "payment_reference must be 100 characters or fewer")
    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("receipt_file_id")
    private Long receiptFileId;

    @Size(max = 4000, message = "description must be 4000 characters or fewer")
    private String description;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "client_request_id must be a UUID"
    )
    @JsonProperty("client_request_id")
    private String clientRequestId;
}
