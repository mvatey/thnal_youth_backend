package org.example.tnal_youth_backend.dashboard.dto;

import java.math.BigDecimal;

public record DonationMetricResponse(
        BigDecimal value,
        String currency,
        BigDecimal changePercent
) {
}