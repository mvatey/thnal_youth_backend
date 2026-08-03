package org.example.tnal_youth_backend.activity.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeListItemResponse {
    private Long activityId;
    private String activityTitleKm;
    private String activityTitleEn;
    private Long branchId;
    private String branchNameKm;
    private String branchNameEn;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private long donorCount;
    private BigDecimal totalKhr;
    private BigDecimal totalUsd;
    private BigDecimal overallTotalUsd;
    private OffsetDateTime latestReceivedAt;
}
