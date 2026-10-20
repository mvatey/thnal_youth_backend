package org.example.tnal_youth_backend.activity.expense.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.expense.dto.request.ActivityExpenseFamilyRequest;
import org.example.tnal_youth_backend.activity.expense.dto.response.ActivityExpenseFamilyResponse;
import org.example.tnal_youth_backend.activity.expense.service.ActivityExpenseService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/*
 * The families of a gift-distribution activity: each family is the name of
 * one of its members plus why it was chosen, and the expense rows recorded
 * on it say what it received.
 */
@RestController
@RequestMapping("/api/activities/{activityId}/expense-families")
@RequiredArgsConstructor
public class ActivityExpenseFamilyController {

    private final ActivityExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ActivityExpenseFamilyResponse>> getFamilies(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(expenseService.getFamilies(activityId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARY', 'BRANCH_LEADER')")
    public ResponseEntity<ActivityExpenseFamilyResponse> createFamily(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityExpenseFamilyRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        expenseService.createFamily(
                                activityId,
                                request,
                                currentUserId(authentication)
                        )
                );
    }

    @PutMapping("/{familyId}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'BRANCH_LEADER')")
    public ResponseEntity<ActivityExpenseFamilyResponse> updateFamily(
            @PathVariable Long activityId,
            @PathVariable Long familyId,
            @Valid @RequestBody ActivityExpenseFamilyRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                expenseService.updateFamily(
                        activityId,
                        familyId,
                        request,
                        currentUserId(authentication)
                )
        );
    }

    @DeleteMapping("/{familyId}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'BRANCH_LEADER')")
    public ResponseEntity<Void> deleteFamily(
            @PathVariable Long activityId,
            @PathVariable Long familyId,
            Authentication authentication
    ) {
        expenseService.deleteFamily(
                activityId,
                familyId,
                currentUserId(authentication)
        );

        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return details.getUserId();
    }
}
