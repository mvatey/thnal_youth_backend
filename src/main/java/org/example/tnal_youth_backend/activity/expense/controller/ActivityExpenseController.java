package org.example.tnal_youth_backend.activity.expense.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.expense.dto.request.CreateActivityExpenseRequest;
import org.example.tnal_youth_backend.activity.expense.dto.request.UpdateActivityExpenseRequest;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseResponse;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseSummaryResponse;
import org.example.tnal_youth_backend.activity.expense.service.ActivityExpenseService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(
        "/api/activities/{activityId}/expenses"
)
@RequiredArgsConstructor
public class ActivityExpenseController {

    private final ActivityExpenseService
            expenseService;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<ActivityExpenseResponse>
    createExpense(
            @PathVariable Long activityId,
            @Valid
            @RequestBody CreateActivityExpenseRequest request,
            Authentication authentication
    ) {
        ActivityExpenseResponse response =
                expenseService.createExpense(
                        activityId,
                        request,
                        getCurrentUserId(authentication)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivityExpenseResponse>>
    getExpenses(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                expenseService.getExpenses(activityId)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<ActivityExpenseSummaryResponse>
    getSummary(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                expenseService.getSummary(activityId)
        );
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ActivityExpenseResponse>
    getExpense(
            @PathVariable Long activityId,
            @PathVariable Long expenseId
    ) {
        return ResponseEntity.ok(
                expenseService.getExpense(
                        activityId,
                        expenseId
                )
        );
    }

    @PutMapping("/{expenseId}")
    @PreAuthorize(
            "hasAnyRole('SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<ActivityExpenseResponse>
    updateExpense(
            @PathVariable Long activityId,
            @PathVariable Long expenseId,
            @Valid
            @RequestBody UpdateActivityExpenseRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                expenseService.updateExpense(
                        activityId,
                        expenseId,
                        request,
                        getCurrentUserId(authentication)
                )
        );
    }

    @DeleteMapping("/{expenseId}")
    @PreAuthorize(
            "hasAnyRole('SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long activityId,
            @PathVariable Long expenseId,
            Authentication authentication
    ) {
        expenseService.deleteExpense(
                activityId,
                expenseId,
                getCurrentUserId(authentication)
        );

        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal
                instanceof CustomUserDetails userDetails) {

            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }
}
