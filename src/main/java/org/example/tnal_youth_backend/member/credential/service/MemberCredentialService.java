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

    /**
     * Whether this member already holds an ACTIVITY_CERTIFICATE for the
     * given activity. Access is checked against the ACTIVITY's host
     * branch (same carve-out as {@link #create}), not the member's own
     * branch — otherwise this check itself would 403 for exactly the
     * cross-branch members the create-time exception exists for, and a
     * caller falling back to "assume no duplicate" on that 403 would
     * defeat the whole point of checking first.
     */
    boolean hasActivityCertificate(
            Long memberId,
            Long activityId
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