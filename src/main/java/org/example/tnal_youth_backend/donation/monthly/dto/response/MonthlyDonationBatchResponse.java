package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class MonthlyDonationBatchResponse {
    private Long branchId;
    private LocalDate donationPeriod;
    private OffsetDateTime paidAt;
    private BigDecimal exchangeRateKhrPerUsd;
    private int savedCount;
    private BigDecimal totalKhr;
    private BigDecimal totalUsd;
    private BigDecimal overallTotalUsd;
    private List<MonthlyDonationSavedItemResponse> items;
}
