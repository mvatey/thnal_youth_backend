package org.example.tnal_youth_backend.member.family.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyResponse;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.example.tnal_youth_backend.member.family.mapper.MemberFamilyMapper;
import org.example.tnal_youth_backend.member.family.repository.MemberFamilyRepository;
import org.example.tnal_youth_backend.member.family.service.MemberFamilyService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberFamilyServiceImpl
        implements MemberFamilyService {

    private final MemberFamilyRepository memberFamilyRepository;
    private final MemberRepository memberRepository;
    private final MemberFamilyMapper memberFamilyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberFamilyResponse> getFamilyByMemberId(
            Long memberId
    ) {
        verifyMemberExists(memberId);

        return memberFamilyRepository
                .findAllByMemberIdOrderByIdAsc(memberId)
                .stream()
                .map(memberFamilyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MemberFamilyResponse createFamilyRecord(
            Long memberId,
            MemberFamilyRequest request
    ) {
        Member member = findMember(memberId);

        MemberFamily family = MemberFamily.builder()
                .member(member)
                .relationship(request.relationship())
                .fullNameKm(
                        normalizeRequired(
                                request.fullNameKm(),
                                "Khmer full name"
                        )
                )
                .fullNameEn(
                        trimToNull(request.fullNameEn())
                )
                .dateOfBirth(request.dateOfBirth())
                .occupation(
                        trimToNull(request.occupation())
                )
                .lifeStatus(request.lifeStatus())
                .address(
                        trimToNull(request.address())
                )
                .build();

        MemberFamily savedFamily =
                memberFamilyRepository.save(family);

        return memberFamilyMapper.toResponse(savedFamily);
    }

    @Override
    @Transactional
    public MemberFamilyResponse updateFamilyRecord(
            Long memberId,
            Long familyId,
            MemberFamilyRequest request
    ) {
        MemberFamily family =
                findFamilyRecord(memberId, familyId);

        family.setRelationship(request.relationship());
        family.setFullNameKm(
                normalizeRequired(
                        request.fullNameKm(),
                        "Khmer full name"
                )
        );
        family.setFullNameEn(
                trimToNull(request.fullNameEn())
        );
        family.setDateOfBirth(request.dateOfBirth());
        family.setOccupation(
                trimToNull(request.occupation())
        );
        family.setLifeStatus(request.lifeStatus());
        family.setAddress(
                trimToNull(request.address())
        );

        MemberFamily updatedFamily =
                memberFamilyRepository.save(family);

        return memberFamilyMapper.toResponse(updatedFamily);
    }

    @Override
    @Transactional
    public void deleteFamilyRecord(
            Long memberId,
            Long familyId
    ) {
        MemberFamily family =
                findFamilyRecord(memberId, familyId);

        memberFamilyRepository.delete(family);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private void verifyMemberExists(Long memberId) {
        findMember(memberId);
    }

    private MemberFamily findFamilyRecord(
            Long memberId,
            Long familyId
    ) {
        if (familyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family record ID is required"
            );
        }

        return memberFamilyRepository
                .findByIdAndMemberId(
                        familyId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Family record not found with ID: "
                                        + familyId
                                        + " for member ID: "
                                        + memberId
                        )
                );
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}