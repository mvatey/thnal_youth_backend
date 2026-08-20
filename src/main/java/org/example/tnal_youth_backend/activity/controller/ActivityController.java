package org.example.tnal_youth_backend.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.request.UpdateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityBranchResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityPageResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.service.ActivityInvitedBranchService;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityInvitedBranchService activityInvitedBranchService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole( 'SECRETARY', 'BRANCH_LEADER')")
    public ActivityResponse createActivity(
            @Valid @RequestBody CreateActivityRequest request,
            Authentication authentication
    ) {
        Long currentUserId =
                extractCurrentUserId(authentication);

        return activityService.createActivity(
                request,
                currentUserId
        );
    }

    @PutMapping("/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')")
    public ActivityResponse updateActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody UpdateActivityRequest request,
            Authentication authentication
    ) {
        Long currentUserId =
                extractCurrentUserId(authentication);

        return activityService.updateActivity(
                activityId,
                request,
                currentUserId
        );
    }

    @PatchMapping("/{activityId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')")
    public ActivityResponse completeActivity(
            @PathVariable Long activityId,
            Authentication authentication
    ) {
        return activityService.completeActivity(
                activityId,
                extractCurrentUserId(authentication)
        );
    }

    @GetMapping("/{activityId}")
    public ActivityResponse getActivityById(
            @PathVariable Long activityId,
            Authentication authentication
    ) {
        return activityService.getActivityById(
                activityId,
                extractCurrentUserId(authentication)
        );
    }

    /**
     * Every branch connected to this activity — the organizer plus every
     * invited branch — each tagged with its role (ORGANIZER / INVITED). One
     * combined list instead of separately reading the activity's own
     * branchId and calling {@code GET .../invited-branches}. See {@link
     * ActivityBranchResponse}.
     */
    @GetMapping("/{activityId}/branches")
    public List<ActivityBranchResponse> getActivityBranches(
            @PathVariable Long activityId
    ) {
        return activityInvitedBranchService.getActivityBranches(
                activityId
        );
    }

    @GetMapping
    public ActivityPageResponse getActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Short sectorId,
            @RequestParam(required = false) Short typeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            /*
             * When provided, narrows the list to exactly that one branch's
             * own-hosted activities plus activities it was invited to and
             * accepted — regardless of the caller's normal role-based
             * scope. Lets a caller (e.g. the activity-donation module) that
             * lets staff pick any ONE of their accessible branches ask for
             * that specific branch's activities.
             */
            @RequestParam(required = false) Long branchId,
            Authentication authentication
    ) {
        return activityService.getActivities(
                page,
                size,
                search,
                sectorId,
                typeId,
                date,
                branchId,
                extractCurrentUserId(authentication)
        );
    }

    private Long extractCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        /*
         * Replace this section with the same logic
         * already used in your current create endpoint.
         */
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.example.tnal_youth_backend.authentication.security.CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }

}
