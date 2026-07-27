package org.example.tnal_youth_backend.dashboard.service;

import org.example.tnal_youth_backend.dashboard.dto.ActivityTypeBreakdownResponse;
import org.example.tnal_youth_backend.dashboard.dto.DashboardActivitiesResponse;
import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;
import org.example.tnal_youth_backend.dashboard.dto.ParticipationTrendResponse;

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
}