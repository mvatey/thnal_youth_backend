package org.example.tnal_youth_backend.repository;

import org.example.tnal_youth_backend.model.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long> {

}