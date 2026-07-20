package org.example.tnal_youth_backend.member.credential.service;

import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;

import java.util.List;

public interface MemberCredentialService {

    List<MemberCredentialResponse> getAllByMemberId(
            Long memberId
    );

    MemberCredentialResponse getById(
            Long memberId,
            Long credentialId
    );

    MemberCredentialResponse create(
            Long memberId,
            MemberCredentialRequest request
    );

    MemberCredentialResponse update(
            Long memberId,
            Long credentialId,
            MemberCredentialRequest request
    );

    void delete(
            Long memberId,
            Long credentialId
    );
}