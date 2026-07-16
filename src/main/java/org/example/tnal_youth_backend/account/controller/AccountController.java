package org.example.tnal_youth_backend.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tnal_youth_backend.account.dto.AccountDto;
import org.example.tnal_youth_backend.account.entity.Account;
import org.example.tnal_youth_backend.account.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts API", description = "Manage organizational accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping
    public Account createAccount(@RequestBody AccountDto accountDto) {
        return accountService.createAccount(accountDto.getAccountCode(), accountDto.getRoleName());
    }
}
