package org.example.tnal_youth_backend.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tnal_youth_backend.account.dto.AccountStatusDto;
import org.example.tnal_youth_backend.account.entity.AccountStatus;
import org.example.tnal_youth_backend.account.service.AccountStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-statuses")
@Tag(name = "Account Statuses API", description = "Manage account statuses")
public class AccountStatusController {
    private final AccountStatusService accountStatusService;

    public AccountStatusController(AccountStatusService accountStatusService) {
        this.accountStatusService = accountStatusService;
    }

    @GetMapping
    public List<AccountStatus> getAllStatuses() {
        return accountStatusService.getAllStatuses();
    }

    @PostMapping
    public AccountStatus createStatus(@RequestBody AccountStatusDto dto) {
        return accountStatusService.createStatus(dto.getStatusName(), dto.getDescription());
    }
}

