package org.example.tnal_youth_backend.activity.type.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.type.dto.response.ActivityTypeResponse;
import org.example.tnal_youth_backend.activity.type.service.ActivityTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
@RequiredArgsConstructor
@Tag(
        name = "C. Member Page - Activity-types"
)
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    @GetMapping
    public ResponseEntity<List<ActivityTypeResponse>>
    getActivityTypes() {

        return ResponseEntity.ok(
                activityTypeService.getActiveActivityTypes()
        );
    }
}