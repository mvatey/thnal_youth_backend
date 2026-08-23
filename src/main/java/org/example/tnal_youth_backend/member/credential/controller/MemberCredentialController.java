package org.example.tnal_youth_backend.member.credential.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialTabResponse;
import org.example.tnal_youth_backend.member.credential.service.MemberCredentialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/members/{memberId}/credentials"
)
@RequiredArgsConstructor
@Tag(
        name = "3.1 Member Page - Credentials",
        description = "Manage credentials for a selected member"
)
public class MemberCredentialController {

    private final MemberCredentialService
            memberCredentialService;

    /*
     * ==========================================================
     * READ CREDENTIAL TAB
     * ==========================================================
     *
     * Returns:
     * - membership card
     * - activity certificates
     * - appointment letters
     */
    @GetMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER',
            'MEMBER',
            'VIEWER'
        )
        """)
    public ResponseEntity<MemberCredentialTabResponse>
    getAll(
            @PathVariable
            Long memberId
    ) {
        MemberCredentialTabResponse credentials =
                memberCredentialService
                        .getCredentialTab(
                                memberId
                        );

        return ResponseEntity.ok(
                credentials
        );
    }

    /*
     * ==========================================================
     * READ ONE CREDENTIAL
     * ==========================================================
     */
    @GetMapping("/{credentialId}")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER',
            'MEMBER',
            'VIEWER'
        )
        """)
    public ResponseEntity<MemberCredentialResponse>
    getById(
            @PathVariable
            Long memberId,

            @PathVariable
            Long credentialId
    ) {
        MemberCredentialResponse credential =
                memberCredentialService
                        .getById(
                                memberId,
                                credentialId
                        );

        return ResponseEntity.ok(
                credential
        );
    }

    /*
     * ==========================================================
     * CREATE CREDENTIAL
     * ==========================================================
     *
     * Detailed rules are handled in the service:
     *
     * MEMBERSHIP_CARD
     * - cannot be manually created
     * - created automatically after member creation
     *
     * ACTIVITY_CERTIFICATE
     * - secretary permission required
     * - member must have attended the activity
     *
     * APPOINTMENT_LETTER
     * - manually created by an authorized management role
     */
    @PostMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberCredentialResponse>
    create(
            @PathVariable
            Long memberId,

            @Valid
            @RequestBody
            MemberCredentialRequest request
    ) {
        MemberCredentialResponse credential =
                memberCredentialService
                        .create(
                                memberId,
                                request
                        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        credential
                );
    }

    /*
     * ==========================================================
     * UPDATE CREDENTIAL
     * ==========================================================
     */
    @PutMapping("/{credentialId}")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<MemberCredentialResponse>
    update(
            @PathVariable
            Long memberId,

            @PathVariable
            Long credentialId,

            @Valid
            @RequestBody
            MemberCredentialRequest request
    ) {
        MemberCredentialResponse credential =
                memberCredentialService
                        .update(
                                memberId,
                                credentialId,
                                request
                        );

        return ResponseEntity.ok(
                credential
        );
    }

    /*
     * ==========================================================
     * DELETE CREDENTIAL
     * ==========================================================
     *
     * Membership cards cannot be deleted.
     * Exact credential-kind permission is validated by the service.
     */
    @DeleteMapping("/{credentialId}")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<Void>
    delete(
            @PathVariable
            Long memberId,

            @PathVariable
            Long credentialId
    ) {
        memberCredentialService
                .delete(
                        memberId,
                        credentialId
                );

        return ResponseEntity
                .noContent()
                .build();
    }
}
