package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberFamilyDto;
import org.example.tnal_youth_backend.member.entity.MemberFamily;
import org.example.tnal_youth_backend.member.service.MemberFamilyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-families")
public class MemberFamilyController {

    private final MemberFamilyService service;

    public MemberFamilyController(MemberFamilyService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberFamily> getFamiliesByMember(@PathVariable Long memberId) {
        return service.getFamiliesByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberFamily getFamilyById(@PathVariable Long id) {
        return service.getFamilyById(id);
    }

    @PostMapping
    public MemberFamily createFamily(@RequestBody MemberFamilyDto dto) {
        return service.createFamily(dto);
    }

    @PutMapping("/{id}")
    public MemberFamily updateFamily(@PathVariable Long id, @RequestBody MemberFamilyDto dto) {
        return service.updateFamily(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFamily(@PathVariable Long id) {
        service.deleteFamily(id);
        return ResponseEntity.noContent().build();
    }
}
