package org.example.tnal_youth_backend.member.member.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberStatusRequest;
import org.example.tnal_youth_backend.member.member.dto.response.*;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Member",
        description = "Manage member information"
)
public class MemberController {

    private final MemberService memberService;

    /*
     * Normal Member table endpoint.
     *
     * GET /api/members
     */
    @GetMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberPageResponse>
    getMembers(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "15")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Short statusId,

            @RequestParam(required = false)
            Gender gender
    ) {
        return ResponseEntity.ok(
                memberService.getMembers(
                        page,
                        size,
                        search,
                        branchId,
                        statusId,
                        gender
                )
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberSummaryResponse>
    getMemberSummary() {

        return ResponseEntity.ok(
                memberService.getMemberSummary()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailResponse>
    getMemberById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                memberService.getMemberById(
                        id
                )
        );
    }

    @PostMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberDetailResponse>
    createMember(
            @Valid
            @RequestBody
            CreateMemberRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        memberService.createMember(
                                request
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDetailResponse>
    updateMember(
            @PathVariable
            Long id,

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

    @PatchMapping("/{id}/status")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberDetailResponse>
    updateMemberStatus(
            @PathVariable Long id,
            @Valid @RequestBody
            UpdateMemberStatusRequest request
    ) {
        return ResponseEntity.ok(
                memberService.updateMemberStatus(
                        id,
                        request
                )
        );
    }

    @GetMapping("/{memberId}/summary")
    public ResponseEntity<MemberDetailSummaryResponse> getMemberDetailSummary(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberService.getMemberDetailSummary(memberId)
        );
    }
}
