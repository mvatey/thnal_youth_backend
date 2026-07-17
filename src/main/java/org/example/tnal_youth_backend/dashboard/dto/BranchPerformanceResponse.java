package org.example.tnal_youth_backend.dashboard.dto;

public record BranchPerformanceResponse(
        String period,
        BranchInfo branch,
        MetricResponse totalActivities,
        DonationMetricResponse totalDonations,
        MetricResponse newMembers
) {

    public record BranchInfo(
            Long id,
            String nameKm,
            String nameEn
    ) {
    }
}