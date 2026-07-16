package org.example.tnal_youth_backend.account.service;

import org.example.tnal_youth_backend.account.entity.AccountStatus;
import org.example.tnal_youth_backend.account.repository.AccountStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountStatusService {
    private final AccountStatusRepository accountStatusRepository;

    public AccountStatusService(AccountStatusRepository accountStatusRepository) {
        this.accountStatusRepository = accountStatusRepository;
    }

    public List<AccountStatus> getAllStatuses() {
        return accountStatusRepository.findAll();
    }

    public AccountStatus createStatus(String statusName, String description) {
        AccountStatus status = new AccountStatus();
        status.setStatusName(statusName);
        status.setDescription(description);
        return accountStatusRepository.save(status);
    }
}
