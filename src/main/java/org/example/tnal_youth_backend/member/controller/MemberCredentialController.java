package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberCredentialDto;
import org.example.tnal_youth_backend.member.entity.MemberCredential;
import org.example.tnal_youth_backend.member.service.MemberCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-credentials")
public class MemberCredentialController {

    private final MemberCredentialService service;

    public MemberCredentialController(MemberCredentialService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberCredential> getCredentialsByMember(@PathVariable Long memberId) {
        return service.getCredentialsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberCredential getCredentialById(@PathVariable Long id) {
        return service.getCredentialById(id);
    }

    @PostMapping
    public MemberCredential createCredential(@RequestBody MemberCredentialDto dto) {
        return service.createCredential(dto);
    }

    @PutMapping("/{id}")
    public MemberCredential updateCredential(@PathVariable Long id, @RequestBody MemberCredentialDto dto) {
        return service.updateCredential(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable Long id) {
        service.deleteCredential(id);
        return ResponseEntity.noContent().build();
    }
}
