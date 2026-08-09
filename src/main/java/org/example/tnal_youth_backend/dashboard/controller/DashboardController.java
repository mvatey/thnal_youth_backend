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
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Long branchId
    ) {
        return ResponseEntity.ok(
                dashboardService.getSummary(month, branchId)
        );
    }

    @GetMapping("/activities")
    public ResponseEntity<DashboardActivitiesResponse>
    getActivities(@RequestParam(required = false) Long branchId) {

        return ResponseEntity.ok(
                dashboardService.getActivities(branchId)
        );
    }

    @GetMapping("/activity-breakdown")
    public ResponseEntity<ActivityTypeBreakdownResponse>
    getActivityTypeBreakdown(
            @RequestParam(required = false)
            String month,
            @RequestParam(required = false) Long branchId
    ) {
        return ResponseEntity.ok(
                dashboardService
                        .getActivityTypeBreakdown(month, branchId)
        );
    }
    @GetMapping("/participation-trend")
    public ResponseEntity<ParticipationTrendResponse>
    getParticipationTrend(
                    @RequestParam(required = false)
                    Integer year,
                    @RequestParam(required = false) Long branchId
            ) {
        return ResponseEntity.ok(
                dashboardService
                        .getParticipationTrend(year, branchId)
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
