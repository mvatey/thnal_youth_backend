package org.example.tnal_youth_backend.member.language.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.language.service.MemberLanguageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/languages")
@RequiredArgsConstructor
@Tag(
        name = "3.0.5.1 Member Page - Languages",
        description = "Manage languages for a selected member"
)
public class MemberLanguageController {

    private final MemberLanguageService languageService;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<List<MemberLanguageResponse>>
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                languageService.getByMemberId(
                        memberId
                )
        );
    }

    @PostMapping
    @PreAuthorize("""
            hasAnyRole(
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberLanguageResponse>
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        languageService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{languageId}")
    @PreAuthorize("""
            hasAnyRole(
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberLanguageResponse>
    update(
            @PathVariable Long memberId,
            @PathVariable Long languageId,

            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity.ok(
                languageService.update(
                        memberId,
                        languageId,
                        request
                )
        );
    }

    @DeleteMapping("/{languageId}")
    @PreAuthorize("""
            hasAnyRole(
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<Void>
    delete(
            @PathVariable Long memberId,
            @PathVariable Long languageId
    ) {
        languageService.delete(
                memberId,
                languageId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PutMapping(
            value = "/{languageId}/certificate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("""
        hasAnyRole(
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberLanguageResponse>
    uploadCertificate(
            @PathVariable Long memberId,
            @PathVariable Long languageId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                languageService.uploadCertificate(
                        memberId,
                        languageId,
                        file
                )
        );
    }

    @DeleteMapping("/{languageId}/certificate")
    @PreAuthorize("""
        hasAnyRole(
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberLanguageResponse>
    removeCertificate(
            @PathVariable Long memberId,
            @PathVariable Long languageId
    ) {
        return ResponseEntity.ok(
                languageService.removeCertificate(
                        memberId,
                        languageId
                )
        );
    }
}