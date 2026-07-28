package org.example.tnal_youth_backend.activity.attendance.mapper;

import org.example.tnal_youth_backend.activity.attendance.dto.response.AttendanceStatusResponse;
import org.example.tnal_youth_backend.activity.attendance.entity.AttendanceStatus;
import org.springframework.stereotype.Component;

@Component
public class AttendanceStatusMapper {

    public AttendanceStatusResponse toResponse(
            AttendanceStatus attendanceStatus
    ) {
        if (attendanceStatus == null) {
            return null;
        }

        return new AttendanceStatusResponse(
                attendanceStatus.getId(),
                attendanceStatus.getCode(),
                attendanceStatus.getLabelKm(),
                attendanceStatus.getLabelEn(),
                attendanceStatus.getDescription(),
                attendanceStatus.getSortOrder()
        );
    }
}