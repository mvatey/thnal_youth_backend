package org.example.tnal_youth_backend.dashboard.controller;

import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;
import org.example.tnal_youth_backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month
    ) {
        YearMonth selectedMonth =
                month != null ? month : YearMonth.now();

        DashboardSummaryResponse response =
                dashboardService.getSummary(selectedMonth);

        return ResponseEntity.ok(response);
    }
}