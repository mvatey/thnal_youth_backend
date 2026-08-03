package org.example.tnal_youth_backend.activity.income.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;
import org.example.tnal_youth_backend.activity.income.service.ActivityIncomeReadService;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/donations/activity")
@RequiredArgsConstructor
public class ActivityIncomeReadController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final ActivityIncomeReadService service;

    @GetMapping
    @PreAuthorize(STAFF)
    public ApiResponse<ActivityIncomePageResponse> list(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime paidFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime paidTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                service.list(branchId, search, paidFrom, paidTo, page, size)
        );
    }

    @GetMapping("/{activityId}")
    @PreAuthorize(STAFF)
    public ApiResponse<ActivityIncomeDetailResponse> getDetail(
            @PathVariable Long activityId
    ) {
        return ApiResponse.ok(service.getDetail(activityId));
    }
}
