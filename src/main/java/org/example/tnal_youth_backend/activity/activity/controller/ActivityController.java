package org.example.tnal_youth_backend.activity.activity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.activity.dto.request.ActivityRequest;
import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.activity.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<List<ActivityResponse>>
    getAllActivities() {

        return ResponseEntity.ok(
                activityService.getAllActivities()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse>
    getActivityById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                activityService.getActivityById(id)
        );
    }

    @PostMapping
    public ResponseEntity<ActivityResponse>
    createActivity(
            @Valid
            @RequestBody
            ActivityRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        activityService
                                .createActivity(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse>
    updateActivity(
            @PathVariable Long id,

            @Valid
            @RequestBody
            ActivityRequest request
    ) {
        return ResponseEntity.ok(
                activityService.updateActivity(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteActivity(
            @PathVariable Long id
    ) {
        activityService.deleteActivity(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}