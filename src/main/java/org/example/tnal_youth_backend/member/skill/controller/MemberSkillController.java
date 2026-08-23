package org.example.tnal_youth_backend.member.skill.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.skill.service.MemberSkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/skills")
@RequiredArgsConstructor
@Tag(
        name = "3.0.5.1 Member Page - Skills",
        description = "Manage skills for a selected member"
)
public class MemberSkillController {

    private final MemberSkillService skillService;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<List<MemberSkillResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                skillService.getByMemberId(
                        memberId
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
    public ResponseEntity<MemberSkillResponse>
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberSkillRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        skillService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{skillId}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberSkillResponse>
    update(
            @PathVariable Long memberId,
            @PathVariable Long skillId,

            @Valid
            @RequestBody
            MemberSkillRequest request
    ) {
        return ResponseEntity.ok(
                skillService.update(
                        memberId,
                        skillId,
                        request
                )
        );
    }

    @DeleteMapping("/{skillId}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<Void>
    delete(
            @PathVariable Long memberId,
            @PathVariable Long skillId
    ) {
        skillService.delete(
                memberId,
                skillId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PutMapping(
            value = "/{skillId}/certificate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberSkillResponse>
    uploadCertificate(
            @PathVariable Long memberId,
            @PathVariable Long skillId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                skillService.uploadCertificate(
                        memberId,
                        skillId,
                        file
                )
        );
    }

    @DeleteMapping("/{skillId}/certificate")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberSkillResponse>
    removeCertificate(
            @PathVariable Long memberId,
            @PathVariable Long skillId
    ) {
        return ResponseEntity.ok(
                skillService.removeCertificate(
                        memberId,
                        skillId
                )
        );
    }
}