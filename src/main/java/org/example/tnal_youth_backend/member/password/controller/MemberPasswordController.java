package org.example.tnal_youth_backend.member.password.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.password.dto.request.ChangeMemberPasswordRequest;
import org.example.tnal_youth_backend.member.password.dto.request.CreateMemberPasswordRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/{memberId}/password")
@RequiredArgsConstructor
@PreAuthorize(
        "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
)
public class MemberPasswordController {

    private final MemberPasswordService memberPasswordService;

    /*
     * Used when opening the Password tab.
     */
    @GetMapping("/status")
    public ResponseEntity<MemberPasswordStatusResponse>
    getPasswordStatus(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService.getPasswordStatus(
                        memberId
                )
        );
    }

    /*
     * Creates the member's first login account and password.
     */
    @PostMapping
    public ResponseEntity<MemberPasswordStatusResponse>
    createPassword(
            @PathVariable Long memberId,
            @Valid
            @RequestBody CreateMemberPasswordRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        memberPasswordService.createPassword(
                                memberId,
                                request
                        )
                );
    }

    /*
     * Changes the selected member's existing password.
     */
    @PatchMapping
    public ResponseEntity<MemberPasswordStatusResponse>
    changePassword(
            @PathVariable Long memberId,
            @Valid
            @RequestBody ChangeMemberPasswordRequest request
    ) {
        return ResponseEntity.ok(
                memberPasswordService.changePassword(
                        memberId,
                        request
                )
        );
    }
}