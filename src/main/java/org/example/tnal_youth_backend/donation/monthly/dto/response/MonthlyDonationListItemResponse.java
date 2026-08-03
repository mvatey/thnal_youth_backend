package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class MonthlyDonationListItemResponse {
    private Long branchId;
    private String branchCode;
    private String branchNameKm;
    private String branchNameEn;
    private LocalDate donationPeriod;
    private Integer donorCount;
    private BigDecimal totalKhr;
    private BigDecimal totalUsd;
    private BigDecimal overallTotalUsd;
    private OffsetDateTime latestPaidAt;
}
