package org.example.tnal_youth_backend.dashboard.service;

import org.example.tnal_youth_backend.dashboard.dto.*;

public interface DashboardService {

    DashboardSummaryResponse getSummary(
            String month,
            Long branchId
    );

    DashboardActivitiesResponse getActivities(Long branchId);

    ActivityTypeBreakdownResponse
    getActivityTypeBreakdown(
            String month,
            Long branchId
    );

    ParticipationTrendResponse
    getParticipationTrend(
            Integer year,
            Long branchId
    );

    BranchPerformanceResponse getBranchPerformance(
            Long branchId,
            String month
    );
}
