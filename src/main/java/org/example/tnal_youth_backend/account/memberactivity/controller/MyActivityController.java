package org.example.tnal_youth_backend.account.memberactivity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberactivity.dto.response.MyActivityResponse;
import org.example.tnal_youth_backend.account.memberactivity.dto.response.MyActivitySummaryResponse;
import org.example.tnal_youth_backend.account.memberactivity.service.MyActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/activities")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - activities",
        description = " កម្មវិធី (my - account ) "
)
public class MyActivityController {

    private final MyActivityService myActivityService;

    @GetMapping
    public ResponseEntity<List<MyActivityResponse>>
    getMyActivities() {

        return ResponseEntity.ok(
                myActivityService.getMyActivities()
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<MyActivitySummaryResponse>
    getMyActivitySummary() {

        return ResponseEntity.ok(
                myActivityService.getMyActivitySummary()
        );
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<MyActivityResponse>
    getMyActivityById(
            @PathVariable Long activityId
    ) {

        return ResponseEntity.ok(
                myActivityService.getMyActivityById(
                        activityId
                )
        );
    }
}