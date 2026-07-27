package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardActivitiesResponse(
        List<DashboardActivityItemResponse> recentCompleted,
        List<DashboardActivityItemResponse> upcoming
) {
}