package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberPoliticalAffiliationDto;
import org.example.tnal_youth_backend.member.entity.MemberPoliticalAffiliation;
import org.example.tnal_youth_backend.member.service.MemberPoliticalAffiliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affiliations")
public class MemberPoliticalAffiliationController {
    private final MemberPoliticalAffiliationService affiliationService;

    public MemberPoliticalAffiliationController(MemberPoliticalAffiliationService affiliationService) {
        this.affiliationService = affiliationService;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberPoliticalAffiliation> getAffiliationsByMember(@PathVariable Long memberId) {
        return affiliationService.getAffiliationsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberPoliticalAffiliation getAffiliationById(@PathVariable Long id) {
        return affiliationService.getAffiliationById(id);
    }

    @PostMapping
    public MemberPoliticalAffiliation createAffiliation(@RequestBody MemberPoliticalAffiliationDto dto) {
        return affiliationService.createAffiliation(dto);
    }

    @PutMapping("/{id}")
    public MemberPoliticalAffiliation updateAffiliation(@PathVariable Long id,
                                                        @RequestBody MemberPoliticalAffiliationDto dto) {
        return affiliationService.updateAffiliation(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteAffiliation(@PathVariable Long id) {
        affiliationService.deleteAffiliation(id);
    }
}

