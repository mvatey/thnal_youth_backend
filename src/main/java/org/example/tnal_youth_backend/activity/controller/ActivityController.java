package org.example.tnal_youth_backend.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityPageResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody
            CreateActivityRequest request,

            @AuthenticationPrincipal
            CustomUserDetails currentUser
    ) {
        ActivityResponse response =
                activityService.createActivity(
                        request,
                        currentUser.getUser().getId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivityById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                activityService.getActivityById(id)
        );
    }

    @GetMapping
    public ResponseEntity<ActivityPageResponse> getActivities(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Short sectorId,

            @RequestParam(required = false)
            Short typeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        ActivityPageResponse response =
                activityService.getActivities(
                        page,
                        size,
                        search,
                        sectorId,
                        typeId,
                        date
                );

        return ResponseEntity.ok(response);
    }
}