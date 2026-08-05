package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyDonationSummaryResponse {
    private Integer memberCount;
    private BigDecimal totalKhr;
    private BigDecimal totalUsd;
    private BigDecimal overallTotalUsd;
}
