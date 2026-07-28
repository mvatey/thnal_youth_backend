package org.example.tnal_youth_backend.activity.attendance.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.attendance.dto.response.AttendanceStatusResponse;
import org.example.tnal_youth_backend.activity.attendance.mapper.AttendanceStatusMapper;
import org.example.tnal_youth_backend.activity.attendance.repository.AttendanceStatusRepository;
import org.example.tnal_youth_backend.activity.attendance.service.AttendanceStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceStatusServiceImpl
        implements AttendanceStatusService {

    private final AttendanceStatusRepository
            attendanceStatusRepository;

    private final AttendanceStatusMapper
            attendanceStatusMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceStatusResponse>
    getActiveAttendanceStatuses() {

        return attendanceStatusRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(attendanceStatusMapper::toResponse)
                .toList();
    }
}