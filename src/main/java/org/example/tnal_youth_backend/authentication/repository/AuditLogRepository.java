package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

}