package org.example.tnal_youth_backend.activity.attendance.service;

import org.example.tnal_youth_backend.activity.attendance.dto.request.AttendanceMemberRequest;
import org.example.tnal_youth_backend.activity.attendance.dto.request.UpdateAttendanceStatusRequest;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendancePageResponse;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendanceResponse;

public interface ActivityAttendanceService {

    ActivityAttendancePageResponse getAttendance(
            Long activityId
    );

    ActivityAttendanceResponse checkIn(
            Long activityId,
            AttendanceMemberRequest request,
            Long currentUserId
    );

    ActivityAttendanceResponse checkOut(
            Long activityId,
            AttendanceMemberRequest request,
            Long currentUserId
    );

    ActivityAttendanceResponse updateStatus(
            Long activityId,
            UpdateAttendanceStatusRequest request,
            Long currentUserId
    );
}