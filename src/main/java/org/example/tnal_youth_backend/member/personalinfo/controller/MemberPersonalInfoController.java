package org.example.tnal_youth_backend.member.personalinfo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.password.dto.request.UpdateMemberRoleRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.example.tnal_youth_backend.member.personalinfo.dto.request.UpdateMemberPersonalInfoRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberPersonalInfoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(
        "/api/members/{memberId}/personal-info"
)
@RequiredArgsConstructor
@Tag(
        name = "3.0.1 Member Page - Personal Information",
        description = """
                View and update personal information, role,
                and account status for a selected member
                """
)
public class MemberPersonalInfoController {

    private final MemberPersonalInfoService
            memberPersonalInfoService;

    private final MemberPasswordService
            memberPasswordService;

    /*
     * ==========================================================
     * PERSONAL INFORMATION
     * ==========================================================
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
    public ResponseEntity<MemberPersonalInfoResponse>
    getPersonalInfo(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPersonalInfoService
                        .getPersonalInfo(
                                memberId
                        )
        );
    }

    @PutMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
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

    /*
     * ==========================================================
     * ACCOUNT INFORMATION
     * ==========================================================
     */

    @GetMapping("/account")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberPasswordStatusResponse>
    getAccountStatus(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .getPasswordStatus(
                                memberId
                        )
        );
    }

    @PatchMapping("/account/role")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberPasswordStatusResponse>
    updateAccountRole(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            UpdateMemberRoleRequest request
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .updateAccountRole(
                                memberId,
                                request
                        )
        );
    }

    @PatchMapping("/account/enable")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberPasswordStatusResponse>
    enableAccount(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .enableAccount(
                                memberId
                        )
        );
    }

    @PatchMapping("/account/disable")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberPasswordStatusResponse>
    disableAccount(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .disableAccount(
                                memberId
                        )
        );
    }

    @PutMapping(
            value = "/cv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("""
        hasAnyRole(
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberPersonalInfoResponse>
    uploadCv(
            @PathVariable Long memberId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                memberPersonalInfoService
                        .uploadCv(
                                memberId,
                                file
                        )
        );
    }
}
