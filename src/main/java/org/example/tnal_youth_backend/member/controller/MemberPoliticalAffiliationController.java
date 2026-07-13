package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberPoliticalAffiliationDto;
import org.example.tnal_youth_backend.member.entity.MemberPoliticalAffiliation;
import org.example.tnal_youth_backend.member.service.MemberPoliticalAffiliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-political-affiliations")
public class MemberPoliticalAffiliationController {

    private final MemberPoliticalAffiliationService service;

    public MemberPoliticalAffiliationController(MemberPoliticalAffiliationService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberPoliticalAffiliation> getAffiliationsByMember(@PathVariable Long memberId) {
        return service.getAffiliationsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberPoliticalAffiliation getAffiliationById(@PathVariable Long id) {
        return service.getAffiliationById(id);
    }

    @PostMapping
    public MemberPoliticalAffiliation createAffiliation(@RequestBody MemberPoliticalAffiliationDto dto) {
        return service.createAffiliation(dto);
    }

    @PutMapping("/{id}")
    public MemberPoliticalAffiliation updateAffiliation(@PathVariable Long id, @RequestBody MemberPoliticalAffiliationDto dto) {
        return service.updateAffiliation(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAffiliation(@PathVariable Long id) {
        service.deleteAffiliation(id);
        return ResponseEntity.noContent().build();
    }
}
