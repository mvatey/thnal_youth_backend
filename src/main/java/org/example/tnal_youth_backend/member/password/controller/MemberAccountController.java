package org.example.tnal_youth_backend.member.password.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.password.dto.request.MemberPasswordResetRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/members/{memberId}/account"
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
        name = "B. Member Page - Account",
        description = "View and manage the selected member's web account"
)
public class MemberAccountController {

    private final MemberPasswordService
            memberPasswordService;

    /*
     * GET /api/members/{memberId}/account/status
     */
    @GetMapping("/status")
    public ResponseEntity<MemberPasswordStatusResponse>
    getAccountStatus(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .getPasswordStatus(
                                memberId
                        )
        );
    }

    /*
     * POST /api/members/{memberId}/account/resend-activation
     */
    @PostMapping("/resend-activation")
    public ResponseEntity<MemberPasswordStatusResponse>
    resendActivationOtp(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .resendActivationOtp(
                                memberId
                        )
        );
    }

    /*
     * PATCH /api/members/{memberId}/account/password
     *
     * Replaces the selected member's password without requiring
     * the previous password.
     */
    @PatchMapping("/password")
    public ResponseEntity<MemberPasswordStatusResponse>
    resetPassword(
            @PathVariable Long memberId,
            @Valid @RequestBody
            MemberPasswordResetRequest request
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .resetPassword(
                                memberId,
                                request
                        )
        );
    }

    /*
     * PATCH /api/members/{memberId}/account/disable
     */
    @PatchMapping("/disable")
    public ResponseEntity<MemberPasswordStatusResponse>
    disableAccount(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .disableAccount(
                                memberId
                        )
        );
    }

    /*
     * PATCH /api/members/{memberId}/account/enable
     */
    @PatchMapping("/enable")
    public ResponseEntity<MemberPasswordStatusResponse>
    enableAccount(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .enableAccount(
                                memberId
                        )
        );
    }
}