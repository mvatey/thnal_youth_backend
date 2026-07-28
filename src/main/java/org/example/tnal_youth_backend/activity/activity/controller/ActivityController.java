package org.example.tnal_youth_backend.activity.activity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.activity.dto.request.ActivityRequest;
import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityListResponse;
import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.activity.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(
        name = "C. Activity",
        description = "Manage activities"
)
public class ActivityController {

    private final ActivityService activityService;

    /*
     * Activity table endpoint.
     *
     * GET /api/activities
     */
    @GetMapping
    public ResponseEntity<List<ActivityListResponse>>
    getAllActivities() {

        return ResponseEntity.ok(
                activityService.getAllActivities()
        );
    }

    /*
     * Search by Khmer or English activity title.
     *
     * GET /api/activities/search?search=Spring
     */
    @GetMapping("/search")
    public ResponseEntity<List<ActivityListResponse>>
    searchActivities(
            @RequestParam
            String search
    ) {
        return ResponseEntity.ok(
                activityService.searchActivities(
                        search
                )
        );
    }

    /*
     * Filter the activity table by activity type.
     *
     * GET /api/activities/filter-by-type?typeId=1
     */
    @GetMapping("/filter-by-type")
    public ResponseEntity<List<ActivityListResponse>>
    filterActivitiesByType(
            @RequestParam
            Short typeId
    ) {
        return ResponseEntity.ok(
                activityService.filterActivitiesByType(
                        typeId
                )
        );
    }

    /*
     * Full activity detail response.
     *
     * GET /api/activities/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse>
    getActivityById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                activityService.getActivityById(
                        id
                )
        );
    }

    /*
     * Create an activity and return the full detail response.
     */
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
                        activityService.createActivity(
                                request
                        )
                );
    }

    /*
     * Update an activity and return the full detail response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse>
    updateActivity(
            @PathVariable
            Long id,

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

    /*
     * Delete an activity.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteActivity(
            @PathVariable
            Long id
    ) {
        activityService.deleteActivity(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}