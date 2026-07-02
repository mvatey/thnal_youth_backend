package org.example.tnal_youth_backend.repository;

import org.example.tnal_youth_backend.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

}