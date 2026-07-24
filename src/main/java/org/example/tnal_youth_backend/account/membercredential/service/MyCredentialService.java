package org.example.tnal_youth_backend.account.membercredential.service;

import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;

import java.util.List;

public interface MyCredentialService {

    List<MemberCredentialResponse> getMyCredentials();

    MemberCredentialResponse getMyCredentialById(
            Long credentialId
    );

    MemberCredentialResponse createMyCredential(
            MemberCredentialRequest request
    );

    MemberCredentialResponse updateMyCredential(
            Long credentialId,
            MemberCredentialRequest request
    );

    void deleteMyCredential(
            Long credentialId
    );
}