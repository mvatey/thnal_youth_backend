package org.example.tnal_youth_backend.dashboard.service;

import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;

import java.time.YearMonth;

public interface DashboardService {

    DashboardSummaryResponse getSummary(YearMonth month);
}