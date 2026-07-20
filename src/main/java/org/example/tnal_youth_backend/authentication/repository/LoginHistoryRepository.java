package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long> {

}