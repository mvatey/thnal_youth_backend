package org.example.tnal_youth_backend.dashboard.dto;

import java.math.BigDecimal;

public record MetricResponse(
        Long value,
        BigDecimal changePercent
) {
}