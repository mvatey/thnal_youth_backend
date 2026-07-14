package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.ChangePasswordDto;
import org.example.tnal_youth_backend.member.dto.ResetPasswordDto;
import org.example.tnal_youth_backend.member.service.MemberAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberAccountController {
    private final MemberAccountService accountService;

    public MemberAccountController(MemberAccountService accountService) {
        this.accountService = accountService;
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordDto dto) {
        accountService.changePassword(id, dto);
        return ResponseEntity.noContent().build();
    }

    //  Fixed: use DTO instead of raw String
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordDto dto) {
        accountService.resetPassword(id, dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
