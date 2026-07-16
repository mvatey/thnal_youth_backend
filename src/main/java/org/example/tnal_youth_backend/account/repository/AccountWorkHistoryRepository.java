package org.example.tnal_youth_backend.account.repository;

import org.example.tnal_youth_backend.account.entity.AccountWorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountWorkHistoryRepository extends JpaRepository<AccountWorkHistory, Long> {
    List<AccountWorkHistory> findByAccountId(Long accountId);
}
