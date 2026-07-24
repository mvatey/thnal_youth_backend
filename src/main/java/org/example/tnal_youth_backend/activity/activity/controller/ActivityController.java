package org.example.tnal_youth_backend.activity.activity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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

    /*
     * Get all activities.
     *
     * GET /api/activities
     */
    @GetMapping
    public ResponseEntity<List<ActivityResponse>>
    getAllActivities() {

        return ResponseEntity.ok(
                activityService.getAllActivities()
        );
    }

    /*
     * Search activities by Khmer or English title.
     *
     * GET /api/activities/search?search=យុវជន
     */
    @GetMapping("/search")
    public ResponseEntity<List<ActivityResponse>>
    searchActivities(
            @RequestParam String search
    ) {
        return ResponseEntity.ok(
                activityService.searchActivities(search)
        );
    }

    /*
     * Filter activities by activity type.
     *
     * GET /api/activities/filter-by-type?typeId=1
     */
    @GetMapping("/filter-by-type")
    public ResponseEntity<List<ActivityResponse>>
    filterActivitiesByType(
            @RequestParam Short typeId
    ) {
        return ResponseEntity.ok(
                activityService.filterActivitiesByType(typeId)
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
                        activityService.createActivity(request)
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