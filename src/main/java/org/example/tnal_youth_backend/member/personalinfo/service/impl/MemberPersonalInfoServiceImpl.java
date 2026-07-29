package org.example.tnal_youth_backend.member.personalinfo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.common.exception.ResourceNotFoundException;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.personalinfo.dto.request.UpdateMemberPersonalInfoRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberPersonalInfoService;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPersonalInfoServiceImpl
        implements MemberPersonalInfoService {

    private final MemberRepository memberRepository;
    private final ReligionRepository religionRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Override
    public MemberPersonalInfoResponse getPersonalInfo(
            Long memberId
    ) {
        Member member = findMember(memberId);

        return toResponse(member);
    }

    @Override
    @Transactional
    public MemberPersonalInfoResponse updatePersonalInfo(
            Long memberId,
            UpdateMemberPersonalInfoRequest request
    ) {
        Member member = findMember(memberId);

        member.setFullNameKm(
                request.fullNameKm().trim()
        );

        member.setFullNameEn(
                normalizeText(request.fullNameEn())
        );

        if (request.gender() != null) {
            member.setGender(request.gender());
        }

        member.setEmail(
                normalizeEmail(request.email())
        );

        member.setPhone(
                normalizeText(request.phone())
        );

        member.setDateOfBirth(
                request.dateOfBirth()
        );

        member.setCurrentAddress(
                normalizeText(request.currentAddress())
        );

        member.setPermanentAddress(
                normalizeText(request.permanentAddress())
        );

        updateReligion(
                member,
                request.religionId()
        );

        updateCvFile(
                member,
                request.cvFileId()
        );

        Member savedMember =
                memberRepository.save(member);

        return toResponse(savedMember);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Member not found with id: " + memberId
                        )
                );
    }

    private void updateReligion(
            Member member,
            Short religionId
    ) {
        if (religionId == null) {
            member.setReligion(null);
            return;
        }

        Religion religion =
                religionRepository.findById(religionId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Religion not found with id: "
                                                + religionId
                                )
                        );

        member.setReligion(religion);
    }

    private void updateCvFile(
            Member member,
            Long cvFileId
    ) {
        if (cvFileId == null) {
            member.setCvFile(null);
            return;
        }

        FileEntity cvFile =
                fileRepository.findById(cvFileId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "File not found with id: "
                                                + cvFileId
                                )
                        );

        member.setCvFile(cvFile);
    }

    private MemberPersonalInfoResponse toResponse(Member member) {

        Short religionId =
                member.getReligion() == null
                        ? null
                        : member.getReligion().getId();

        Long cvFileId =
                member.getCvFile() == null
                        ? null
                        : member.getCvFile().getId();

        User user = userRepository
                .findByMemberId(member.getId())
                .orElse(null);

        Long accountId =
                user == null
                        ? null
                        : user.getId();

        boolean hasAccount = user != null;

        UserStatus accountStatus =
                user == null
                        ? null
                        : user.getStatus();

        return new MemberPersonalInfoResponse(
                member.getId(),
                member.getFullNameKm(),
                member.getFullNameEn(),
                member.getGender(),
                religionId,
                member.getEmail(),
                member.getPhone(),
                member.getDateOfBirth(),
                member.getCurrentAddress(),
                member.getPermanentAddress(),
                cvFileId,
                accountId,
                hasAccount,
                accountStatus
        );
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {
        String normalizedEmail =
                normalizeText(email);

        return normalizedEmail == null
                ? null
                : normalizedEmail.toLowerCase();
    }
}