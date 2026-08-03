package org.example.tnal_youth_backend.activity.income.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.service.ActivityIncomeService;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities/{activityId}/incomes")
@RequiredArgsConstructor
public class ActivityIncomeController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final ActivityIncomeService activityIncomeService;

    @PostMapping("/batch")
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
}
