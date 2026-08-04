package org.example.tnal_youth_backend.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.dto.*;
import org.example.tnal_youth_backend.dashboard.service.DashboardService;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardMonthResolver dashboardMonthResolver;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) String month
    ) {
        return ResponseEntity.ok(
                dashboardService.getSummary(month)
        );
    }

    @GetMapping("/activities")
    public ResponseEntity<DashboardActivitiesResponse>
    getActivities() {

        return ResponseEntity.ok(
                dashboardService.getActivities()
        );
    }

    @GetMapping("/activity-breakdown")
    public ResponseEntity<ActivityTypeBreakdownResponse>
    getActivityTypeBreakdown(
            @RequestParam(required = false)
            String month
    ) {
        return ResponseEntity.ok(
                dashboardService
                        .getActivityTypeBreakdown(month)
        );
    }
    @GetMapping("/participation-trend")
    public ResponseEntity<ParticipationTrendResponse>
    getParticipationTrend(
                    @RequestParam(required = false)
                    Integer year
            ) {
        return ResponseEntity.ok(
                dashboardService
                        .getParticipationTrend(year)
        );
    }

    @GetMapping("/branch-performance")
    public ResponseEntity<BranchPerformanceResponse>
    getBranchPerformance(
            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            String month
    ) {
        return ResponseEntity.ok(
                dashboardService
                        .getBranchPerformance(
                                branchId,
                                month
                        )
        );
    }
}