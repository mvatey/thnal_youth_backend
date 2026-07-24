package org.example.tnal_youth_backend.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.request.UpdateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityPageResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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

    @GetMapping("/{activityId}")
    public ActivityResponse getActivityById(
            @PathVariable Long activityId
    ) {
        return activityService.getActivityById(activityId);
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
            LocalDate date
    ) {
        return activityService.getActivities(
                page,
                size,
                search,
                sectorId,
                typeId,
                date
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