package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberSkillDto;
import org.example.tnal_youth_backend.member.entity.MemberSkill;
import org.example.tnal_youth_backend.member.service.MemberSkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-skills")
public class MemberSkillController {

    private final MemberSkillService service;

    public MemberSkillController(MemberSkillService service) {
        this.service = service;
    }

    @GetMapping("/member/{memberId}")
    public List<MemberSkill> getSkillsByMember(@PathVariable Long memberId) {
        return service.getSkillsByMember(memberId);
    }

    @GetMapping("/{id}")
    public MemberSkill getSkillById(@PathVariable Long id) {
        return service.getSkillById(id);
    }

    @PostMapping
    public MemberSkill createSkill(@RequestBody MemberSkillDto dto) {
        return service.createSkill(dto);
    }

    @PutMapping("/{id}")
    public MemberSkill updateSkill(@PathVariable Long id, @RequestBody MemberSkillDto dto) {
        return service.updateSkill(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        service.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
