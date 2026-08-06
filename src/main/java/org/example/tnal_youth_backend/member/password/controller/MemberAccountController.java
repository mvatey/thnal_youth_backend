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
        name = "3.5 Member Page - Password",
        description = "Manage the selected member's password and activation"
)
public class MemberAccountController {

    private final MemberPasswordService
            memberPasswordService;

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

    @PatchMapping("/password")
    public ResponseEntity<MemberPasswordStatusResponse>
    resetPassword(
            @PathVariable
            Long memberId,

            @Valid
            @RequestBody
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
}