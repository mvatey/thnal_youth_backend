package org.example.tnal_youth_backend.member.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberListResponse>> getAllMembers() {
        return ResponseEntity.ok(
                memberService.getAllMembers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> getMemberById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                memberService.getMemberById(id)
        );
    }

    @PostMapping
    public ResponseEntity<MemberDetailResponse> createMember(
            @Valid
            @RequestBody
            CreateMemberRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        memberService.createMember(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> updateMember(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(
                memberService.updateMember(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long id
    ) {
        memberService.deleteMember(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}