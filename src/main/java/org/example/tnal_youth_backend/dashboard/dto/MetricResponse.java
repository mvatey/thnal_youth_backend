package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MetricResponse(
        long value,
        BigDecimal changePercent
) {
}