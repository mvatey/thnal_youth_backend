package org.example.tnal_youth_backend.activity.income.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;
import org.example.tnal_youth_backend.activity.income.service.ActivityIncomeReadService;
import org.example.tnal_youth_backend.activity.income.service.ActivityIncomeService;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityIncomeController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final ActivityIncomeService activityIncomeService;
    private final ActivityIncomeReadService activityIncomeReadService;

    /*
     * Save Activity Income rows for one activity.
     *
     * POST /api/activities/{activityId}/incomes/batch
     */
    @PostMapping("/{activityId}/incomes/batch")
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<ActivityIncomeBatchResponse>> createBatch(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityIncomeBatchRequest request
    ) {
        ActivityIncomeBatchResponse response =
                activityIncomeService.createBatch(activityId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /*
     * Return the grouped Activity Income list.
     *
     * GET /api/activities/incomes
     */
    @GetMapping("/incomes")
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
        ActivityIncomePageResponse response =
                activityIncomeReadService.list(
                        branchId,
                        search,
                        paidFrom,
                        paidTo,
                        page,
                        size
                );

        return ApiResponse.ok(response);
    }

    /*
     * Return Activity Income details for one activity.
     *
     * GET /api/activities/{activityId}/incomes
     */
    @GetMapping("/{activityId}/incomes")
    @PreAuthorize(STAFF)
    public ApiResponse<ActivityIncomeDetailResponse> getDetail(
            @PathVariable Long activityId
    ) {
        return ApiResponse.ok(
                activityIncomeReadService.getDetail(activityId)
        );
    }
}