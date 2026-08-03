package org.example.tnal_youth_backend.donation.monthly.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyDonationItemRequest {

    @NotNull
    @JsonProperty("member_id")
    private Long memberId;

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

    @Size(max = 100)
    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("receipt_file_id")
    private Long receiptFileId;

    @Size(max = 4000)
    private String description;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "client_request_id must be a UUID"
    )
    @JsonProperty("client_request_id")
    private String clientRequestId;
}
