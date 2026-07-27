package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

@Builder
public record DashboardSummaryData(
        MetricResponse members,
        MetricResponse branches,
        MetricResponse activities,
        DonationMetricResponse donations
) {
}