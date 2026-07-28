package org.example.tnal_youth_backend.activity.attendance.repository;

import org.example.tnal_youth_backend.activity.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceStatusRepository
        extends JpaRepository<AttendanceStatus, Short> {

    Optional<AttendanceStatus> findByCodeIgnoreCase(
            String code
    );

    List<AttendanceStatus>
    findAllByActiveTrueOrderBySortOrderAscIdAsc();
}