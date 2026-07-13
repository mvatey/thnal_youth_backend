package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) { this.service = service; }

    @GetMapping public List<Member> getAll() { return service.getAllMembers(); }
    @GetMapping("/{id}") public Member getById(@PathVariable Long id) { return service.getMemberById(id); }
    @PostMapping public Member create(@RequestBody MemberDto dto) { return service.saveMember(dto); }
    @PutMapping("/{id}") public Member update(@PathVariable Long id, @RequestBody MemberDto dto) { return service.updateMember(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deleteMember(id); return ResponseEntity.noContent().build(); }
}
