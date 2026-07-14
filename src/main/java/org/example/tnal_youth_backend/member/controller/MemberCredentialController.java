package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberCredentialDto;
import org.example.tnal_youth_backend.member.entity.MemberCredential;
import org.example.tnal_youth_backend.member.service.MemberCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credentials")
public class MemberCredentialController {
    private final MemberCredentialService credentialService;

    public MemberCredentialController(MemberCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberCredential> getCredentialsByMember(@PathVariable Long memberId) {
        return credentialService.getCredentialsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberCredential getCredentialById(@PathVariable Long id) {
        return credentialService.getCredentialById(id);
    }

    @PostMapping
    public MemberCredential createCredential(@RequestBody MemberCredentialDto dto) {
        return credentialService.createCredential(dto);
    }

    @PutMapping("/{id}")
    public MemberCredential updateCredential(@PathVariable Long id, @RequestBody MemberCredentialDto dto) {
        return credentialService.updateCredential(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteCredential(@PathVariable Long id) {
        credentialService.deleteCredential(id);
    }
}
