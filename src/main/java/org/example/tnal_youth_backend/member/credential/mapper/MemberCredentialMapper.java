package org.example.tnal_youth_backend.member.credential.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCredentialMapper {

    public MemberCredential toEntity(
            Long memberId,
            MemberCredentialRequest request
    ) {
        MemberCredential credential =
                new MemberCredential();

        credential.setMemberId(
                memberId
        );

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
        if (credential == null) {
            return null;
        }

        MemberCredentialResponse response =
                new MemberCredentialResponse();

        response.setId(
                credential.getId()
        );

        response.setTitle(
                credential.getTitle()
        );

        response.setCredentialNo(
                credential.getCredentialNo()
        );

        response.setIssuedOn(
                credential.getIssuedOn()
        );

        response.setCreatedAt(
                credential.getCreatedAt()
        );

        response.setUpdatedAt(
                credential.getUpdatedAt()
        );

        /*
         * Credential Kind
         */
        response.setCredentialKind(
                toCredentialKindResponse(
                        credential.getCredentialKind()
                )
        );

        /*
         * File
         */
        response.setFile(
                toFileResponse(
                        credential.getFile()
                )
        );

        return response;
    }

    private MemberCredentialResponse.CredentialKindResponse
    toCredentialKindResponse(
            String credentialKind
    ) {
        if (credentialKind == null
                || credentialKind.isBlank()) {

            return null;
        }

        String normalizedKind =
                credentialKind.trim();

        return switch (normalizedKind) {

            case "MEMBERSHIP_CARD" ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            "MEMBERSHIP_CARD",
                            "ប័ណ្ណសមាជិក",
                            "Membership Card"
                    );

            case "CERTIFICATE" ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            "CERTIFICATE",
                            "វិញ្ញាបនបត្រ",
                            "Certificate"
                    );

            case "ID_CARD" ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            "ID_CARD",
                            "អត្តសញ្ញាណប័ណ្ណ",
                            "ID Card"
                    );

            default ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            normalizedKind,
                            normalizedKind,
                            normalizedKind
                    );
        };
    }

    private MemberCredentialResponse.FileResponse
    toFileResponse(
            FileEntity file
    ) {
        if (file == null) {
            return null;
        }

        Long sizeBytes =
                file.getSizeBytes();

        Double sizeKb = null;
        Double sizeMb = null;

        if (sizeBytes != null) {
            sizeKb =
                    Math.round(
                            (
                                    sizeBytes / 1024.0
                            ) * 100.0
                    ) / 100.0;

            sizeMb =
                    Math.round(
                            (
                                    sizeBytes
                                            / (
                                            1024.0
                                                    * 1024.0
                                    )
                            ) * 100.0
                    ) / 100.0;
        }

        return new MemberCredentialResponse.FileResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType(),
                sizeBytes,
                sizeKb,
                sizeMb
        );
    }

    /**
     * Copies request values into the entity.
     */
    private void copyRequestToEntity(
            MemberCredential credential,
            MemberCredentialRequest request
    ) {
        credential.setTitle(
                normalizeRequired(
                        request.getTitle()
                )
        );

        credential.setCredentialKind(
                normalizeRequired(
                        request.getCredentialKind()
                )
        );

        credential.setCredentialNo(
                normalizeOptional(
                        request.getCredentialNo()
                )
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
        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }
}