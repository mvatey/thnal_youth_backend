package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberLanguageDto;
import org.example.tnal_youth_backend.member.entity.MemberLanguage;
import org.example.tnal_youth_backend.member.service.MemberLanguageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-languages")
public class MemberLanguageController {

    private final MemberLanguageService service;

    public MemberLanguageController(MemberLanguageService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberLanguage> getLanguagesByMember(@PathVariable Long memberId) {
        return service.getLanguagesByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberLanguage getLanguageById(@PathVariable Long id) {
        return service.getLanguageById(id);
    }

    @PostMapping
    public MemberLanguage createLanguage(@RequestBody MemberLanguageDto dto) {
        return service.createLanguage(dto);
    }

    @PutMapping("/{id}")
    public MemberLanguage updateLanguage(@PathVariable Long id, @RequestBody MemberLanguageDto dto) {
        return service.updateLanguage(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        service.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }
}
