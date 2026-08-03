package org.example.tnal_youth_backend.activity.expense.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExpenseSummaryResponse {

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("total_records")
    private long totalRecords;

    @JsonProperty("total_khr")
    private BigDecimal totalKhr;

    @JsonProperty("total_usd")
    private BigDecimal totalUsd;

    @JsonProperty("overall_total_usd")
    private BigDecimal overallTotalUsd;
}