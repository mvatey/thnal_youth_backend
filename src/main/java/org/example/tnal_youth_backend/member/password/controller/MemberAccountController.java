package org.example.tnal_youth_backend.member.password.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
     * Returns whether the member has an account and whether
     * it is pending activation, active, inactive, or locked.
     *
     * GET /api/members/{memberId}/account/status
     */
    @GetMapping("/status")
    public ResponseEntity<MemberPasswordStatusResponse>
    getAccountStatus(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .getPasswordStatus(
                                memberId
                        )
        );
    }

    /*
     * Sends another activation OTP to a member whose
     * account is still pending activation.
     *
     * POST /api/members/{memberId}/account/resend-activation
     */
    @PostMapping("/resend-activation")
    public ResponseEntity<MemberPasswordStatusResponse>
    resendActivationOtp(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .resendActivationOtp(
                                memberId
                        )
        );
    }

    /*
     * Disables access without deleting the member or user.
     *
     * PATCH /api/members/{memberId}/account/disable
     */
    @PatchMapping("/disable")
    public ResponseEntity<MemberPasswordStatusResponse>
    disableAccount(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .disableAccount(
                                memberId
                        )
        );
    }

    /*
     * Re-enables a previously inactive account.
     *
     * PATCH /api/members/{memberId}/account/enable
     */
    @PatchMapping("/enable")
    public ResponseEntity<MemberPasswordStatusResponse>
    enableAccount(
            @PathVariable
            Long memberId
    ) {
        return ResponseEntity.ok(
                memberPasswordService
                        .enableAccount(
                                memberId
                        )
        );
    }
}