package org.example.tnal_youth_backend.dashboard.service;

import org.example.tnal_youth_backend.dashboard.dto.*;

public interface DashboardService {

    DashboardSummaryResponse getSummary(
            String month
    );

    DashboardActivitiesResponse getActivities();

    ActivityTypeBreakdownResponse
    getActivityTypeBreakdown(
            String month
    );

    ParticipationTrendResponse
    getParticipationTrend(
            Integer year
    );

    BranchPerformanceResponse getBranchPerformance(
            Long branchId,
            String month
    );
}