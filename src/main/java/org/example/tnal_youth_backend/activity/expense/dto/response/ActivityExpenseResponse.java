package org.example.tnal_youth_backend.activity.expense.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExpenseResponse {

    private Long id;

    @JsonProperty("activity_id")
    private Long activityId;

    private String name;

    private String description;

    private BigDecimal quantity;

    @JsonProperty("amount_khr")
    private BigDecimal amountKhr;

    @JsonProperty("amount_usd")
    private BigDecimal amountUsd;

    @JsonProperty("total_amount_usd")
    private BigDecimal totalAmountUsd;

    @JsonProperty("spent_on")
    private LocalDate spentOn;

    @JsonProperty("receipt_file_id")
    private Long receiptFileId;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}