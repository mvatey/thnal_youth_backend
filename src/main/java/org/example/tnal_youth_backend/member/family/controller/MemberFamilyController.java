package org.example.tnal_youth_backend.member.family.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyInfoRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyInfoResponse;
import org.example.tnal_youth_backend.member.family.service.MemberFamilyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/{memberId}/family")
@RequiredArgsConstructor
@Tag(
        name = "3.0.2 Member Page - Family",
        description = "View and update family information for a selected member"
)
public class MemberFamilyController {

    private final MemberFamilyService memberFamilyService;

    /*
     * GET /api/members/{memberId}/family
     */
    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<MemberFamilyInfoResponse>
    getFamilyInfo(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberFamilyService.getFamilyInfo(
                        memberId
                )
        );
    }

    /*
     * PUT /api/members/{memberId}/family
     */
    @PutMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberFamilyInfoResponse>
    updateFamilyInfo(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberFamilyInfoRequest request
    ) {
        return ResponseEntity.ok(
                memberFamilyService.updateFamilyInfo(
                        memberId,
                        request
                )
        );
    }
}