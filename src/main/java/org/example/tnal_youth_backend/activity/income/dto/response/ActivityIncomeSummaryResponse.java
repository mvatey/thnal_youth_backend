package org.example.tnal_youth_backend.activity.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeSummaryResponse {
    private long memberCount;
    private BigDecimal totalKhr;
    private BigDecimal totalUsd;
    private BigDecimal overallTotalUsd;
}
