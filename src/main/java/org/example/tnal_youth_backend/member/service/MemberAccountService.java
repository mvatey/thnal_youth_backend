package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.ChangePasswordDto;
import org.example.tnal_youth_backend.member.entity.MemberAccount;
import org.example.tnal_youth_backend.member.repository.MemberAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberAccountService {
    private final MemberAccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberAccountService(MemberAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void changePassword(Long memberId, ChangePasswordDto dto) {
        MemberAccount account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    // 👉 New method for reset
    public void resetPassword(Long memberId, String newPassword) {
        MemberAccount account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
