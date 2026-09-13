package org.example.tnal_youth_backend.member.password.service;

import org.example.tnal_youth_backend.member.password.dto.request.MemberPasswordResetRequest;
import org.example.tnal_youth_backend.member.password.dto.request.UpdateMemberRoleRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;

public interface MemberPasswordService {

    MemberPasswordStatusResponse getPasswordStatus(
            Long memberId
    );

    MemberPasswordStatusResponse resendActivationOtp(
            Long memberId
    );

    MemberPasswordStatusResponse resetPassword(
            Long memberId,
            MemberPasswordResetRequest request
    );

    MemberPasswordStatusResponse disableAccount(
            Long memberId
    );

    MemberPasswordStatusResponse enableAccount(
            Long memberId
    );

    /**
     * Sends an ACTIVE (or INACTIVE) account back to PENDING_ACTIVATION --
     * clears the password hash and any lock/failed-login state, and
     * revokes existing sessions, so the account can only come back by
     * completing OTP activation again from scratch. Meant for support
     * cases (e.g. a demo/test account that needs to redo the activation
     * flow) — never for an account already pending activation.
     */
    MemberPasswordStatusResponse resetToPendingActivation(
            Long memberId
    );

    MemberPasswordStatusResponse updateAccountRole(
            Long memberId,
            UpdateMemberRoleRequest request
    );

    MemberPasswordStatusResponse changeOwnPassword(
            Long memberId,
            String oldPassword,
            String newPassword,
            String confirmPassword
    );
}