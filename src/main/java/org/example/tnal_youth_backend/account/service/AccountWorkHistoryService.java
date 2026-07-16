package org.example.tnal_youth_backend.account.service;

import org.example.tnal_youth_backend.account.entity.Account;
import org.example.tnal_youth_backend.account.entity.AccountWorkHistory;
import org.example.tnal_youth_backend.account.repository.AccountRepository;
import org.example.tnal_youth_backend.account.repository.AccountWorkHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountWorkHistoryService {
    private final AccountWorkHistoryRepository workHistoryRepository;
    private final AccountRepository accountRepository;

    public AccountWorkHistoryService(AccountWorkHistoryRepository workHistoryRepository,
                                     AccountRepository accountRepository) {
        this.workHistoryRepository = workHistoryRepository;
        this.accountRepository = accountRepository;
    }

    public List<AccountWorkHistory> getWorkHistoryByAccount(Long accountId) {
        return workHistoryRepository.findByAccountId(accountId);
    }

    public AccountWorkHistory addWorkHistory(Long accountId, String responsibilities,
                                             java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        AccountWorkHistory history = new AccountWorkHistory();
        history.setAccount(account);
        history.setResponsibilities(responsibilities);
        history.setStartDate(startDate);
        history.setEndDate(endDate);
        return workHistoryRepository.save(history);
    }
}
