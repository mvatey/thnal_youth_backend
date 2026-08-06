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

    MemberPasswordStatusResponse updateAccountRole(
            Long memberId,
            UpdateMemberRoleRequest request
    );
}