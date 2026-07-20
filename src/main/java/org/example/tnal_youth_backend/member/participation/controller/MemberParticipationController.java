package org.example.tnal_youth_backend.member.participation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.participation.dto.request.MemberParticipationRequest;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;
import org.example.tnal_youth_backend.member.participation.service.MemberParticipationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/members/{memberId}/participations"
)
@RequiredArgsConstructor
public class MemberParticipationController {

    private final MemberParticipationService
            memberParticipationService;

    @GetMapping
    public ResponseEntity<List<MemberParticipationResponse>>
    getParticipationsByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberParticipationService
                        .getParticipationsByMemberId(memberId)
        );
    }

    @PostMapping
    public ResponseEntity<MemberParticipationResponse>
    create(
            @PathVariable Long memberId,

            @Valid
            @RequestBody
            MemberParticipationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        memberParticipationService.create(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{participationId}")
    public ResponseEntity<MemberParticipationResponse>
    update(
            @PathVariable Long memberId,
            @PathVariable Long participationId,

            @Valid
            @RequestBody
            MemberParticipationRequest request
    ) {
        return ResponseEntity.ok(
                memberParticipationService.update(
                        memberId,
                        participationId,
                        request
                )
        );
    }

    @DeleteMapping("/{participationId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long participationId
    ) {
        memberParticipationService.delete(
                memberId,
                participationId
        );

        return ResponseEntity.noContent().build();
    }
}