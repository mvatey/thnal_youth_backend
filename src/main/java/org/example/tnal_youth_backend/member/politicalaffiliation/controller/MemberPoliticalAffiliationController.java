package org.example.tnal_youth_backend.member.politicalaffiliation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.service.MemberPoliticalAffiliationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/members/{memberId}/political-affiliations"
)
@RequiredArgsConstructor
public class MemberPoliticalAffiliationController {

    private final MemberPoliticalAffiliationService
            affiliationService;

    @GetMapping
    public ResponseEntity<
            List<MemberPoliticalAffiliationResponse>
            > getByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                affiliationService.getByMemberId(
                        memberId
                )
        );
    }

    @GetMapping("/{affiliationId}")
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            > getById(
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
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            > create(
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
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            > update(
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
    public ResponseEntity<Void> delete(
            @PathVariable Long memberId,
            @PathVariable Long affiliationId
    ) {
        affiliationService.delete(
                memberId,
                affiliationId
        );

        return ResponseEntity.noContent().build();
    }
}