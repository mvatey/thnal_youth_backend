package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

@Builder
public record BranchPerformanceResponse(
        String period,
        BranchPerformanceScopeResponse scope,
        MetricResponse activities,
        DonationMetricResponse donations,
        MetricResponse members
) {
}