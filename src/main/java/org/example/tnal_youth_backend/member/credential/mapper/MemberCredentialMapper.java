package org.example.tnal_youth_backend.member.credential.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.entity.CredentialStatus;
import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MemberCredentialMapper {

    public MemberCredential toEntity(
            Long memberId,
            Long issuedById,
            MemberCredentialRequest request
    ) {
        MemberCredential credential =
                new MemberCredential();

        credential.setMemberId(memberId);
        credential.setIssuedById(issuedById);
        credential.setStatus(CredentialStatus.ACTIVE);

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

        response.setMemberId(
                credential.getMemberId()
        );

        response.setActivityId(
                credential.getActivityId()
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

        response.setStatus(
                credential.getStatus()
        );

        response.setCreatedAt(
                credential.getCreatedAt()
        );

        response.setUpdatedAt(
                credential.getUpdatedAt()
        );

        response.setCredentialKind(
                toCredentialKindResponse(
                        credential.getCredentialKind()
                )
        );

        response.setFile(
                toFileResponse(
                        credential.getFile()
                )
        );

        if (credential.getIssuedBy() != null) {
            response.setIssuedBy(
                    new MemberCredentialResponse.IssuedByResponse(
                            credential.getIssuedBy().getId(),
                            resolveIssuerName(credential)
                    )
            );
        }

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
                normalizeCredentialKind(
                        credentialKind
                );

        return switch (normalizedKind) {

            case "MEMBERSHIP_CARD" ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            "MEMBERSHIP_CARD",
                            "ប័ណ្ណសម្គាល់សមាជិក",
                            "Membership Card"
                    );

            case "ACTIVITY_CERTIFICATE" ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            "ACTIVITY_CERTIFICATE",
                            "បណ្ណសរសើរ",
                            "Activity Certificate"
                    );

            case "APPOINTMENT_LETTER" ->
                    new MemberCredentialResponse
                            .CredentialKindResponse(
                            "APPOINTMENT_LETTER",
                            "លិខិតតែងតាំង",
                            "Appointment Letter"
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
                            sizeBytes / 1024.0 * 100.0
                    ) / 100.0;

            sizeMb =
                    Math.round(
                            sizeBytes
                                    / (1024.0 * 1024.0)
                                    * 100.0
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
                normalizeCredentialKind(
                        request.getCredentialKind()
                )
        );

        credential.setCredentialNo(
                normalizeRequired(
                        request.getCredentialNo()
                )
        );

        credential.setActivityId(
                request.getActivityId()
        );

        credential.setIssuedOn(
                request.getIssuedOn()
        );

        credential.setFileId(
                request.getFileId()
        );
    }

    private String normalizeCredentialKind(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeRequired(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private String resolveIssuerName(
            MemberCredential credential
    ) {
        if (credential.getIssuedBy() == null) {
            return null;
        }

        /*
         * Replace this with the actual available user/member
         * name field in your User entity.
         */
        return credential
                .getIssuedBy()
                .getEmail();
    }
}