package org.example.tnal_youth_backend.account.repository;

import org.example.tnal_youth_backend.account.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, Long> {
    Optional<AccountStatus> findByStatusName(String statusName);
}
