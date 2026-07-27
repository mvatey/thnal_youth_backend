package org.example.tnal_youth_backend.dashboard.service;

import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;

public interface DashboardService {

    DashboardSummaryResponse getSummary(String month);
}