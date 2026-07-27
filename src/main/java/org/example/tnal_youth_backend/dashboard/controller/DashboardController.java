package org.example.tnal_youth_backend.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;
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

    /*
     * Temporary endpoint.
     * Remove after confirming the month parsing works.
     */
    @GetMapping("/test-month")
    public ResponseEntity<DashboardMonthRange> testMonth(
            @RequestParam(required = false) String month
    ) {
        return ResponseEntity.ok(
                dashboardMonthResolver.resolve(month)
        );
    }
}