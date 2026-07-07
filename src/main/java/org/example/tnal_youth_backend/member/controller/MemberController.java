package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.MemberDto;
import org.example.tnal_youth_backend.member.entity.Member;
import org.example.tnal_youth_backend.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;   // ✅ Import this
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllMembers() {
        return memberService.getAllMembers().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMemberById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return toResponse(member);
    }

    @PostMapping
    public Map<String, Object> createMember(@RequestBody MemberDto dto) {
        Member member = memberService.saveMember(dto);
        return toResponse(member);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateMember(@PathVariable Long id, @RequestBody MemberDto dto) {
        Member member = memberService.updateMember(id, dto);
        return toResponse(member);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }

    // Helper method to shape response
    private Map<String, Object> toResponse(Member member) {
        Map<String, Object> response = new LinkedHashMap<>(); // ✅ preserves order
        response.put("id", member.getId());
        response.put("memberNo", member.getMemberNo());

        // Put fullNameEn and fullNameKh together
        response.put("fullNameEn", member.getFullNameEn());
        response.put("fullNameKh", member.getFullNameKh());

        response.put("gender", member.getGender());
        response.put("dateOfBirth", member.getDateOfBirth());
        response.put("phone", member.getPhone());
        response.put("email", member.getEmail());
        response.put("address", member.getAddress());
        response.put("bio", member.getBio());

        if (member.getBranch() != null) {
            response.put("branchCode", member.getBranch().getBranchCode());
            response.put("branchName", member.getBranch().getNameEn());
        }
        if (member.getPosition() != null) {
            response.put("positionCode", member.getPosition().getCode());
            response.put("positionLabel", member.getPosition().getLabelEn());
        }
        if (member.getStatus() != null) {
            response.put("statusCode", member.getStatus().getCode());
            response.put("statusLabel", member.getStatus().getLabelEn());
        }

        return response;
    }
}
