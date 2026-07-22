package org.example.tnal_youth_backend.member.password.service;

import org.example.tnal_youth_backend.member.password.dto.request.ChangeMemberPasswordRequest;
import org.example.tnal_youth_backend.member.password.dto.request.CreateMemberPasswordRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;

public interface MemberPasswordService {

    MemberPasswordStatusResponse getPasswordStatus(
            Long memberId
    );

    MemberPasswordStatusResponse createPassword(
            Long memberId,
            CreateMemberPasswordRequest request
    );

    MemberPasswordStatusResponse changePassword(
            Long memberId,
            ChangeMemberPasswordRequest request
    );
}