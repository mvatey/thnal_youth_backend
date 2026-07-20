package org.example.tnal_youth_backend.activity.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.attendance.dto.response.AttendanceStatusResponse;
import org.example.tnal_youth_backend.activity.attendance.service.AttendanceStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance-statuses")
@RequiredArgsConstructor
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