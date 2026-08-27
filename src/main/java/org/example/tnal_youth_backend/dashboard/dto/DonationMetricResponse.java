package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DonationMetricResponse(
        BigDecimal amountUsd,
        BigDecimal changePercent
) {
}
