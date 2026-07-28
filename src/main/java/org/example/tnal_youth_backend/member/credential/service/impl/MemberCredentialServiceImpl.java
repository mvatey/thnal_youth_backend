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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCredentialServiceImpl
        implements MemberCredentialService {

    private final MemberCredentialRepository memberCredentialRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final MemberCredentialMapper memberCredentialMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberCredentialResponse> getAllByMemberId(
            Long memberId
    ) {
        validateMemberExists(memberId);

        return memberCredentialRepository
                .findAllByMemberIdOrderByCreatedAtDesc(memberId)
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
                findCredential(memberId, credentialId);

        return memberCredentialMapper.toResponse(credential);
    }

    @Override
    public MemberCredentialResponse create(
            Long memberId,
            MemberCredentialRequest request
    ) {
        validateMemberExists(memberId);
        validateFileExists(request.getFileId());

        String normalizedCredentialNo =
                normalizeCredentialNo(request.getCredentialNo());

        if (normalizedCredentialNo != null
                && memberCredentialRepository
                .existsByCredentialNo(normalizedCredentialNo)) {

            throw new IllegalArgumentException(
                    "Credential number already exists: "
                            + normalizedCredentialNo
            );
        }

        request.setCredentialNo(normalizedCredentialNo);

        MemberCredential credential =
                memberCredentialMapper.toEntity(
                        memberId,
                        request
                );

        MemberCredential savedCredential =
                memberCredentialRepository.save(credential);

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
        validateFileExists(request.getFileId());

        MemberCredential credential =
                findCredential(memberId, credentialId);

        String normalizedCredentialNo =
                normalizeCredentialNo(request.getCredentialNo());

        if (normalizedCredentialNo != null
                && memberCredentialRepository
                .existsByCredentialNoAndIdNot(
                        normalizedCredentialNo,
                        credentialId
                )) {

            throw new IllegalArgumentException(
                    "Credential number already exists: "
                            + normalizedCredentialNo
            );
        }

        request.setCredentialNo(normalizedCredentialNo);

        memberCredentialMapper.updateEntity(
                credential,
                request
        );

        MemberCredential savedCredential =
                memberCredentialRepository.save(credential);

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
                findCredential(memberId, credentialId);

        memberCredentialRepository.delete(credential);
    }

    private MemberCredential findCredential(
            Long memberId,
            Long credentialId
    ) {
        return memberCredentialRepository
                .findByIdAndMemberId(
                        credentialId,
                        memberId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Member credential not found. "
                                        + "memberId="
                                        + memberId
                                        + ", credentialId="
                                        + credentialId
                        )
                );
    }

    private void validateMemberExists(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException(
                    "Member ID is required"
            );
        }

        if (!memberRepository.existsById(memberId)) {
            throw new RuntimeException(
                    "Member not found with id: " + memberId
            );
        }
    }

    private void validateFileExists(Long fileId) {
        if (fileId == null) {
            return;
        }

        if (!fileRepository.existsById(fileId)) {
            throw new RuntimeException(
                    "File not found with id: " + fileId
            );
        }
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