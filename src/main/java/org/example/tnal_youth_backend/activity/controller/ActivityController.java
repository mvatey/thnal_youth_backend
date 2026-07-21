package org.example.tnal_youth_backend.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}