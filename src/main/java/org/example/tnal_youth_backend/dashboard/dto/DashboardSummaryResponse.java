package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

@Builder
public record DashboardSummaryResponse(
        String period,
        DashboardSummaryData summary
) {
}