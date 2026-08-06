package org.example.tnal_youth_backend.member.participation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.participation.dto.request.MemberParticipationRequest;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationPageResponse;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;
import org.example.tnal_youth_backend.member.participation.service.MemberParticipationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/members/{memberId}/participations"
)
@RequiredArgsConstructor
@PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
@Tag(
        name = "3.2 Member Page - Participation",
        description = "Manage participation for a selected member"
)
public class MemberParticipationController {

    private final MemberParticipationService
            memberParticipationService;

    /*
     * Member participation table.
     *
     * GET /api/members/{memberId}/participations
     *     ?page=0
     *     &size=10
     *     &search=meeting
     *     &typeId=1
     */
    @GetMapping
    public ResponseEntity<MemberParticipationPageResponse>
    getParticipationsByMemberId(
            @PathVariable
            Long memberId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Short typeId,

            @RequestParam(required = false)
                    Short attendanceStatusId
    ) {
        return ResponseEntity.ok(
                memberParticipationService
                        .getParticipationsByMemberId(
                                memberId,
                                page,
                                size,
                                search,
                                typeId,
                                attendanceStatusId
                        )
        );
    }

    @PostMapping
    public ResponseEntity<MemberParticipationResponse>
    create(
            @PathVariable
            Long memberId,

            @Valid
            @RequestBody
            MemberParticipationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        memberParticipationService
                                .create(
                                        memberId,
                                        request
                                )
                );
    }

    @PutMapping("/{participationId}")
    public ResponseEntity<MemberParticipationResponse>
    update(
            @PathVariable
            Long memberId,

            @PathVariable
            Long participationId,

            @Valid
            @RequestBody
            MemberParticipationRequest request
    ) {
        return ResponseEntity.ok(
                memberParticipationService
                        .update(
                                memberId,
                                participationId,
                                request
                        )
        );
    }

    @DeleteMapping("/{participationId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            Long memberId,

            @PathVariable
            Long participationId
    ) {
        memberParticipationService.delete(
                memberId,
                participationId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}