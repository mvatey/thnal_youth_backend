package org.example.tnal_youth_backend.member.family.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyResponse;
import org.example.tnal_youth_backend.member.family.service.MemberFamilyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/family")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Family",
        description = "Manage family information for a selected member"
)
public class MemberFamilyController {

    private final MemberFamilyService memberFamilyService;

    @GetMapping
    public ResponseEntity<List<MemberFamilyResponse>>
    getFamilyByMemberId(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberFamilyService.getFamilyByMemberId(memberId)
        );
    }

    @PostMapping
    public ResponseEntity<MemberFamilyResponse>
    createFamilyRecord(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberFamilyRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        memberFamilyService.createFamilyRecord(
                                memberId,
                                request
                        )
                );
    }

    @PutMapping("/{familyId}")
    public ResponseEntity<MemberFamilyResponse>
    updateFamilyRecord(
            @PathVariable Long memberId,
            @PathVariable Long familyId,
            @Valid @RequestBody MemberFamilyRequest request
    ) {
        return ResponseEntity.ok(
                memberFamilyService.updateFamilyRecord(
                        memberId,
                        familyId,
                        request
                )
        );
    }

    @DeleteMapping("/{familyId}")
    public ResponseEntity<Void>
    deleteFamilyRecord(
            @PathVariable Long memberId,
            @PathVariable Long familyId
    ) {
        memberFamilyService.deleteFamilyRecord(
                memberId,
                familyId
        );

        return ResponseEntity.noContent().build();
    }
}