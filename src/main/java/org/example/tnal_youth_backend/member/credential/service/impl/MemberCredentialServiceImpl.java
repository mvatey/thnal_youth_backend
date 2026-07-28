package org.example.tnal_youth_backend.member.credential.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.entity.MemberCredential;
import org.example.tnal_youth_backend.member.credential.mapper.MemberCredentialMapper;
import org.example.tnal_youth_backend.member.credential.repository.MemberCredentialRepository;
import org.example.tnal_youth_backend.member.credential.service.MemberCredentialService;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCredentialServiceImpl
        implements MemberCredentialService {

    private static final String MEMBERSHIP_CARD_KIND =
            "ID_CARD";

    private static final String APPOINTMENT_LETTER_KIND =
            "APPOINTMENT_LETTER";

    private static final String CERTIFICATE_KIND =
            "CERTIFICATE";

    private final MemberCredentialRepository
            memberCredentialRepository;

    private final MemberRepository
            memberRepository;

    private final FileRepository
            fileRepository;

    private final MemberCredentialMapper
            memberCredentialMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberCredentialResponse>
    getAllByMemberId(
            Long memberId
    ) {
        validateMemberExists(memberId);

        return memberCredentialRepository
                .findAllByMemberIdOrderByCreatedAtDesc(
                        memberId
                )
                .stream()
                .map(memberCredentialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberCredentialResponse getById(
            Long memberId,
            Long credentialId
    ) {
        validateMemberExists(memberId);

        MemberCredential credential =
                findCredential(
                        memberId,
                        credentialId
                );

        return memberCredentialMapper
                .toResponse(credential);
    }

    @Override
    public MemberCredentialResponse create(
            Long memberId,
            MemberCredentialRequest request
    ) {
        validateMemberExists(memberId);

        validateFileExists(
                request.getFileId()
        );

        String normalizedCredentialKind =
                normalizeCredentialKind(
                        request.getCredentialKind()
                );

        validateSingleIdCardOnCreate(
                memberId,
                normalizedCredentialKind
        );

        String normalizedCredentialNo =
                normalizeCredentialNo(
                        request.getCredentialNo()
                );

        validateUniqueCredentialNoForCreate(
                normalizedCredentialNo
        );

        request.setCredentialKind(
                normalizedCredentialKind
        );

        request.setCredentialNo(
                normalizedCredentialNo
        );

        MemberCredential credential =
                memberCredentialMapper.toEntity(
                        memberId,
                        request
                );

        MemberCredential savedCredential =
                memberCredentialRepository.save(
                        credential
                );

        return memberCredentialMapper.toResponse(
                savedCredential
        );
    }

    @Override
    public MemberCredentialResponse update(
            Long memberId,
            Long credentialId,
            MemberCredentialRequest request
    ) {
        validateMemberExists(memberId);

        validateFileExists(
                request.getFileId()
        );

        MemberCredential credential =
                findCredential(
                        memberId,
                        credentialId
                );

        String normalizedCredentialKind =
                normalizeCredentialKind(
                        request.getCredentialKind()
                );

        validateSingleIdCardOnUpdate(
                memberId,
                credentialId,
                normalizedCredentialKind
        );

        String normalizedCredentialNo =
                normalizeCredentialNo(
                        request.getCredentialNo()
                );

        validateUniqueCredentialNoForUpdate(
                credentialId,
                normalizedCredentialNo
        );

        request.setCredentialKind(
                normalizedCredentialKind
        );

        request.setCredentialNo(
                normalizedCredentialNo
        );

        memberCredentialMapper.updateEntity(
                credential,
                request
        );

        MemberCredential savedCredential =
                memberCredentialRepository.save(
                        credential
                );

        return memberCredentialMapper.toResponse(
                savedCredential
        );
    }

    @Override
    public void delete(
            Long memberId,
            Long credentialId
    ) {
        validateMemberExists(memberId);

        MemberCredential credential =
                findCredential(
                        memberId,
                        credentialId
                );

        memberCredentialRepository.delete(
                credential
        );
    }

    private MemberCredential findCredential(
            Long memberId,
            Long credentialId
    ) {
        if (credentialId == null
                || credentialId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential ID must be greater than zero"
            );
        }

        return memberCredentialRepository
                .findByIdAndMemberId(
                        credentialId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member credential not found. "
                                        + "memberId="
                                        + memberId
                                        + ", credentialId="
                                        + credentialId
                        )
                );
    }

    private void validateMemberExists(
            Long memberId
    ) {
        if (memberId == null
                || memberId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID must be greater than zero"
            );
        }

        if (!memberRepository.existsById(
                memberId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found with ID: "
                            + memberId
            );
        }
    }

    private void validateFileExists(
            Long fileId
    ) {
        if (fileId == null) {
            return;
        }

        if (fileId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File ID must be greater than zero"
            );
        }

        if (!fileRepository.existsById(
                fileId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File not found with ID: "
                            + fileId
            );
        }
    }

    private void validateSingleIdCardOnCreate(
            Long memberId,
            String credentialKind
    ) {
        if (!MEMBERSHIP_CARD_KIND.equals(
                credentialKind
        )) {
            return;
        }

        boolean alreadyHasIdCard =
                memberCredentialRepository
                        .existsByMemberIdAndCredentialKindIgnoreCase(
                                memberId,
                                MEMBERSHIP_CARD_KIND
                        );

        if (alreadyHasIdCard) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has an ID card"
            );
        }
    }

    private void validateSingleIdCardOnUpdate(
            Long memberId,
            Long credentialId,
            String credentialKind
    ) {
        if (!MEMBERSHIP_CARD_KIND.equals(
                credentialKind
        )) {
            return;
        }

        boolean alreadyHasAnotherIdCard =
                memberCredentialRepository
                        .existsByMemberIdAndCredentialKindIgnoreCaseAndIdNot(
                                memberId,
                                MEMBERSHIP_CARD_KIND,
                                credentialId
                        );

        if (alreadyHasAnotherIdCard) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has another ID card"
            );
        }
    }

    private void validateUniqueCredentialNoForCreate(
            String credentialNo
    ) {
        if (credentialNo == null) {
            return;
        }

        if (memberCredentialRepository
                .existsByCredentialNo(
                        credentialNo
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Credential number already exists: "
                            + credentialNo
            );
        }
    }

    private void validateUniqueCredentialNoForUpdate(
            Long credentialId,
            String credentialNo
    ) {
        if (credentialNo == null) {
            return;
        }

        if (memberCredentialRepository
                .existsByCredentialNoAndIdNot(
                        credentialNo,
                        credentialId
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Credential number already exists: "
                            + credentialNo
            );
        }
    }

    private String normalizeCredentialKind(
            String credentialKind
    ) {
        if (credentialKind == null
                || credentialKind.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential kind is required"
            );
        }

        String normalizedKind =
                credentialKind
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (normalizedKind) {
            case "MEMBERSHIP_CARD",
                 "ACTIVITY_CERTIFICATE" ->
                    normalizedKind;

            default ->
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unsupported credential kind: "
                                    + credentialKind
                    );
        };
    }

    private String normalizeCredentialNo(
            String credentialNo
    ) {
        if (credentialNo == null
                || credentialNo.isBlank()) {
            return null;
        }

        return credentialNo.trim();
    }
}