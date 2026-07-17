package org.example.tnal_youth_backend.dashboard.dto;

import java.math.BigDecimal;

public record MetricResponse(
        BigDecimal value,
        BigDecimal changePercent
) {
}