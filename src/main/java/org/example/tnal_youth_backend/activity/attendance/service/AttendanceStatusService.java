package org.example.tnal_youth_backend.activity.attendance.service;

import org.example.tnal_youth_backend.activity.attendance.dto.response.AttendanceStatusResponse;

import java.util.List;

public interface AttendanceStatusService {

    List<AttendanceStatusResponse>
    getActiveAttendanceStatuses();
}