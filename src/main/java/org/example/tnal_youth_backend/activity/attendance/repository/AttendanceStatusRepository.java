package org.example.tnal_youth_backend.activity.attendance.repository;

import org.example.tnal_youth_backend.activity.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceStatusRepository
        extends JpaRepository<AttendanceStatus, Short> {

    List<AttendanceStatus>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}