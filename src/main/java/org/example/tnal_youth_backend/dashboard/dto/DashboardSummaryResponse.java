package org.example.tnal_youth_backend.dashboard.dto;

public record DashboardSummaryResponse(
        String period,
        MetricResponse totalMembers,
        MetricResponse totalBranches,
        MetricResponse totalActivities,
        DonationMetricResponse totalDonations
) {
}