package org.example.tnal_youth_backend.activity.status.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.status.dto.response.ActivityStatusResponse;
import org.example.tnal_youth_backend.activity.status.service.ActivityStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-statuses")
@RequiredArgsConstructor
public class ActivityStatusController {

    private final ActivityStatusService activityStatusService;

    @GetMapping
    public ResponseEntity<List<ActivityStatusResponse>>
    getActivityStatuses() {

        return ResponseEntity.ok(
                activityStatusService
                        .getActiveActivityStatuses()
        );
    }
}