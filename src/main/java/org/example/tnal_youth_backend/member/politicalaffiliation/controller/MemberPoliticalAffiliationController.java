package org.example.tnal_youth_backend.member.politicalaffiliation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.service.MemberPoliticalAffiliationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/members/{memberId}/political-affiliations"
)
@RequiredArgsConstructor
@Tag(
        name = "3.0.6 Member Page - Political Affiliations",
        description =
                "Manage political affiliations for a selected member"
)
public class MemberPoliticalAffiliationController {

    private final MemberPoliticalAffiliationService
            affiliationService;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<
            List<MemberPoliticalAffiliationResponse>
            >
    getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                affiliationService.getByMemberId(
                        memberId
                )
        );
    }

    @GetMapping("/{affiliationId}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            >
    getById(
            @PathVariable Long memberId,
            @PathVariable Long affiliationId
    ) {
        return ResponseEntity.ok(
                affiliationService.getById(
                        memberId,
                        affiliationId
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
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            >
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberPoliticalAffiliationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        affiliationService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{affiliationId}")
    @PreAuthorize("""
            hasAnyRole(
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            >
    update(
            @PathVariable Long memberId,
            @PathVariable Long affiliationId,

            @Valid
            @RequestBody
            MemberPoliticalAffiliationRequest request
    ) {
        return ResponseEntity.ok(
                affiliationService.update(
                        memberId,
                        affiliationId,
                        request
                )
        );
    }

    @DeleteMapping("/{affiliationId}")
    @PreAuthorize("""
            hasAnyRole(
                'SECRETARY',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<Void>
    delete(
            @PathVariable Long memberId,
            @PathVariable Long affiliationId
    ) {
        affiliationService.delete(
                memberId,
                affiliationId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}