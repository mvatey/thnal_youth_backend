package org.example.tnal_youth_backend.member.personalinfo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.personalinfo.dto.request.UpdateMemberPersonalInfoRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberPersonalInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/members/{memberId}/personal-info"
)
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Personal Information",
        description = "View and update a selected member's personal information"
)
public class MemberPersonalInfoController {

    private final MemberPersonalInfoService
            memberPersonalInfoService;

    @GetMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER',
            'MEMBER'
        )
        """)
    public ResponseEntity<MemberPersonalInfoResponse>
    getPersonalInfo(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPersonalInfoService
                        .getPersonalInfo(memberId)
        );
    }

    @PutMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER',
            'MEMBER'
        )
        """)
    public ResponseEntity<MemberPersonalInfoResponse>
    updatePersonalInfo(
            @PathVariable
            Long memberId,

            @Valid
            @RequestBody
            UpdateMemberPersonalInfoRequest request
    ) {
        return ResponseEntity.ok(
                memberPersonalInfoService
                        .updatePersonalInfo(
                                memberId,
                                request
                        )
        );
    }
}