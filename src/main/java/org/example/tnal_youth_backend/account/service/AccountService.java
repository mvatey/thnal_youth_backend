package org.example.tnal_youth_backend.account.service;

import org.example.tnal_youth_backend.account.entity.Account;
import org.example.tnal_youth_backend.account.entity.Role;
import org.example.tnal_youth_backend.account.repository.AccountRepository;
import org.example.tnal_youth_backend.account.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(String accountCode, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Account account = new Account();
        account.setAccountCode(accountCode);
        account.setRole(role);
        return accountRepository.save(account);
    }
}
