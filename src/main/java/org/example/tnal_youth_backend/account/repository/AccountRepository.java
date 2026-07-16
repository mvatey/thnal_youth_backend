package org.example.tnal_youth_backend.account.repository;

import org.example.tnal_youth_backend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {}
