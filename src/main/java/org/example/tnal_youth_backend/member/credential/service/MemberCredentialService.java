package org.example.tnal_youth_backend.member.credential.service;

import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialTabResponse;

import java.util.List;

public interface MemberCredentialService {

    MemberCredentialTabResponse getCredentialTab(
            Long memberId
    );

    MemberCredentialResponse createDefaultMembershipCard(
            Long memberId,
            Long issuedById
    );

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