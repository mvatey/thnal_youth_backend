package org.example.tnal_youth_backend.member.credential.mapper;

import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.springframework.stereotype.Component;

@Component
public class MemberCredentialMapper {

    public MemberCredential toEntity(
            Long memberId,
            MemberCredentialRequest request
    ) {
        MemberCredential credential = new MemberCredential();

        credential.setMemberId(memberId);

        copyRequestToEntity(
                credential,
                request
        );

        return credential;
    }

    public void updateEntity(
            MemberCredential credential,
            MemberCredentialRequest request
    ) {
        copyRequestToEntity(
                credential,
                request
        );
    }

    public MemberCredentialResponse toResponse(
            MemberCredential credential
    ) {
        MemberCredentialResponse response =
                new MemberCredentialResponse();

        response.setId(credential.getId());
        response.setMemberId(credential.getMemberId());
        response.setTitle(credential.getTitle());
        response.setCredentialKind(credential.getCredentialKind());
        response.setCredentialNo(credential.getCredentialNo());
        response.setIssuedOn(credential.getIssuedOn());
        response.setIssuedById(credential.getIssuedById());
        response.setFileId(credential.getFileId());
        response.setCreatedAt(credential.getCreatedAt());
        response.setUpdatedAt(credential.getUpdatedAt());

        return response;
    }

    /**
     * Copies request values into an entity.
     */
    private void copyRequestToEntity(
            MemberCredential credential,
            MemberCredentialRequest request
    ) {
        credential.setTitle(
                normalizeRequired(request.getTitle())
        );

        credential.setCredentialKind(
                normalizeRequired(request.getCredentialKind())
        );

        credential.setCredentialNo(
                normalizeOptional(request.getCredentialNo())
        );

        credential.setIssuedOn(
                request.getIssuedOn()
        );

        credential.setIssuedById(
                request.getIssuedById()
        );

        credential.setFileId(
                request.getFileId()
        );
    }

    private String normalizeRequired(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}