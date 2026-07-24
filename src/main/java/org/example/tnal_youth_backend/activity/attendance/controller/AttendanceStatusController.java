package org.example.tnal_youth_backend.activity.attendance.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.attendance.dto.response.AttendanceStatusResponse;
import org.example.tnal_youth_backend.activity.attendance.service.AttendanceStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance-statuses")
@RequiredArgsConstructor
@Tag(
        name = "C. Member Page - attendance-status"
)
public class AttendanceStatusController {

    private final AttendanceStatusService
            attendanceStatusService;

    @GetMapping
    public ResponseEntity<List<AttendanceStatusResponse>>
    getAttendanceStatuses() {

        return ResponseEntity.ok(
                attendanceStatusService
                        .getActiveAttendanceStatuses()
        );
    }
}