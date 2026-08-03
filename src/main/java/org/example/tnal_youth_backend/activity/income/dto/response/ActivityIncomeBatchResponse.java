package org.example.tnal_youth_backend.activity.income.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeBatchResponse {

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("activity_name")
    private String activityName;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("received_at")
    private OffsetDateTime receivedAt;

    @JsonProperty("exchange_rate_khr_per_usd")
    private BigDecimal exchangeRateKhrPerUsd;

    @JsonProperty("saved_count")
    private Integer savedCount;

    @JsonProperty("total_khr")
    private BigDecimal totalKhr;

    @JsonProperty("total_usd")
    private BigDecimal totalUsd;

    @JsonProperty("overall_total_usd")
    private BigDecimal overallTotalUsd;

    private List<ActivityIncomeSavedItemResponse> items;
}
