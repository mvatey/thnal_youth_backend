package org.example.tnal_youth_backend.activity.attendance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.attendance.dto.request.AttendanceMemberRequest;
import org.example.tnal_youth_backend.activity.attendance.dto.request.UpdateAttendanceStatusRequest;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendancePageResponse;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendanceResponse;
import org.example.tnal_youth_backend.activity.attendance.service.ActivityAttendanceService;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(
        "/api/activities/{activityId}/attendance"
)
@RequiredArgsConstructor
public class ActivityAttendanceController {

    private final ActivityAttendanceService
            attendanceService;

    @GetMapping
    public ResponseEntity<ActivityAttendancePageResponse>
    getAttendance(
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendance(
                        activityId
                )
        );
    }

    @PostMapping("/check-in")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<ActivityAttendanceResponse>
    checkIn(
            @PathVariable Long activityId,
            @Valid
            @RequestBody AttendanceMemberRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                attendanceService.checkIn(
                        activityId,
                        request,
                        getCurrentUserId(authentication)
                )
        );
    }

    @PostMapping("/check-out")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<ActivityAttendanceResponse>
    checkOut(
            @PathVariable Long activityId,
            @Valid
            @RequestBody AttendanceMemberRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                attendanceService.checkOut(
                        activityId,
                        request,
                        getCurrentUserId(authentication)
                )
        );
    }

    @PatchMapping("/status")
    @PreAuthorize(
            "hasAnyRole('SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<ActivityAttendanceResponse>
    updateStatus(
            @PathVariable Long activityId,
            @Valid
            @RequestBody UpdateAttendanceStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                attendanceService.updateStatus(
                        activityId,
                        request,
                        getCurrentUserId(authentication)
                )
        );
    }

    private Long getCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal
                instanceof CustomUserDetails userDetails) {

            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }
}