package org.example.tnal_youth_backend.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tnal_youth_backend.account.dto.AccountWorkHistoryDto;
import org.example.tnal_youth_backend.account.entity.AccountWorkHistory;
import org.example.tnal_youth_backend.account.service.AccountWorkHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-work-history")
@Tag(name = "Account Work History API", description = "Manage account work history")
public class AccountWorkHistoryController {
    private final AccountWorkHistoryService workHistoryService;

    public AccountWorkHistoryController(AccountWorkHistoryService workHistoryService) {
        this.workHistoryService = workHistoryService;
    }

    @GetMapping("/{accountId}")
    public List<AccountWorkHistory> getWorkHistory(@PathVariable Long accountId) {
        return workHistoryService.getWorkHistoryByAccount(accountId);
    }

    @PostMapping
    public AccountWorkHistory addWorkHistory(@RequestBody AccountWorkHistoryDto dto) {
        return workHistoryService.addWorkHistory(dto.getAccountId(), dto.getResponsibilities(),
                dto.getStartDate(), dto.getEndDate());
    }
}
