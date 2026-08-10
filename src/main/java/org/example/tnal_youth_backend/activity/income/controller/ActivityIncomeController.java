package org.example.tnal_youth_backend.activity.income.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.MemberActivityIncomeHistoryResponse;
import org.example.tnal_youth_backend.activity.income.service.ActivityIncomeService;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityIncomeController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final ActivityIncomeService activityIncomeService;

    @GetMapping("/incomes/members/{memberId}")
    @PreAuthorize(STAFF)
    public ApiResponse<MemberActivityIncomeHistoryResponse> getMemberHistory(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ApiResponse.ok(
                activityIncomeService.getMemberHistory(memberId, page, size)
        );
    }

    @PostMapping("/{activityId}/incomes/batch")
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<ActivityIncomeBatchResponse>> createBatch(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityIncomeBatchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        activityIncomeService.createBatch(activityId, request)
                ));
    }

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
        return ApiResponse.ok(activityIncomeService.list(
                branchId,
                search,
                paidFrom,
                paidTo,
                page,
                size
        ));
    }

    @GetMapping("/{activityId}/incomes")
    @PreAuthorize(STAFF)
    public ApiResponse<ActivityIncomeDetailResponse> getDetail(
            @PathVariable Long activityId
    ) {
        return ApiResponse.ok(activityIncomeService.getDetail(activityId));
    }

    @DeleteMapping("/{activityId}/incomes/{donationId}")
    @PreAuthorize(STAFF)
    public ResponseEntity<Void> deleteIncome(
            @PathVariable Long activityId,
            @PathVariable Long donationId
    ) {
        activityIncomeService.deleteIncome(activityId, donationId);
        return ResponseEntity.noContent().build();
    }
}
