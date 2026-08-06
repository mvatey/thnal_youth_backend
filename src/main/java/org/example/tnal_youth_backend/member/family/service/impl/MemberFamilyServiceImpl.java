package org.example.tnal_youth_backend.member.family.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.family.dto.request.FamilyPersonRequest;
import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyInfoRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyInfoResponse;
import org.example.tnal_youth_backend.member.family.entity.FamilyRelationship;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.example.tnal_youth_backend.member.family.mapper.MemberFamilyMapper;
import org.example.tnal_youth_backend.member.family.repository.MemberFamilyRepository;
import org.example.tnal_youth_backend.member.family.service.MemberFamilyService;
import org.example.tnal_youth_backend.member.member.entity.MaritalStatus;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFamilyServiceImpl
        implements MemberFamilyService {

    private final MemberFamilyRepository
            memberFamilyRepository;

    private final MemberRepository
            memberRepository;

    private final MemberFamilyMapper
            memberFamilyMapper;

    private final MemberAccessValidator
            memberAccessValidator;

    /*
     * ==========================================================
     * GET FAMILY INFORMATION
     * ==========================================================
     */

    @Override
    public MemberFamilyInfoResponse getFamilyInfo(
            Long memberId
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        Member member =
                findMember(
                        memberId
                );

        MemberFamily father =
                findFamilyByRelationship(
                        memberId,
                        FamilyRelationship.FATHER
                );

        MemberFamily mother =
                findFamilyByRelationship(
                        memberId,
                        FamilyRelationship.MOTHER
                );

        MemberFamily spouse =
                findFamilyByRelationship(
                        memberId,
                        FamilyRelationship.SPOUSE
                );

        return new MemberFamilyInfoResponse(
                member.getId(),
                member.getMaritalStatus(),
                memberFamilyMapper.toResponse(
                        father
                ),
                memberFamilyMapper.toResponse(
                        mother
                ),
                memberFamilyMapper.toResponse(
                        spouse
                )
        );
    }

    /*
     * ==========================================================
     * UPDATE FAMILY INFORMATION
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberFamilyInfoResponse updateFamilyInfo(
            Long memberId,
            MemberFamilyInfoRequest request
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family information request is required"
            );
        }

        Member member =
                findMember(
                        memberId
                );

        MaritalStatus maritalStatus =
                request.maritalStatus();

        if (maritalStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Marital status is required"
            );
        }

        member.setMaritalStatus(
                maritalStatus
        );

        upsertFamilyPerson(
                member,
                FamilyRelationship.FATHER,
                request.father()
        );

        upsertFamilyPerson(
                member,
                FamilyRelationship.MOTHER,
                request.mother()
        );

        if (
                maritalStatus
                        == MaritalStatus.SINGLE
        ) {
            /*
             * SINGLE members should not retain a spouse record.
             */
            deleteFamilyByRelationship(
                    memberId,
                    FamilyRelationship.SPOUSE
            );
        } else {
            upsertFamilyPerson(
                    member,
                    FamilyRelationship.SPOUSE,
                    request.spouse()
            );
        }

        memberRepository.saveAndFlush(
                member
        );

        memberFamilyRepository.flush();

        return buildResponse(
                member
        );
    }

    /*
     * ==========================================================
     * CREATE OR UPDATE FAMILY PERSON
     * ==========================================================
     */

    private void upsertFamilyPerson(
            Member member,
            FamilyRelationship relationship,
            FamilyPersonRequest request
    ) {
        /*
         * An empty section removes an existing family record.
         */
        if (isEmptyPerson(request)) {
            deleteFamilyByRelationship(
                    member.getId(),
                    relationship
            );

            return;
        }

        MemberFamily family =
                memberFamilyRepository
                        .findByMember_IdAndRelationship(
                                member.getId(),
                                relationship
                        )
                        .orElseGet(() ->
                                MemberFamily
                                        .builder()
                                        .member(member)
                                        .relationship(
                                                relationship
                                        )
                                        .build()
                        );

        family.setFullNameKm(
                normalizeRequired(
                        request.fullNameKm(),
                        getRelationshipLabel(
                                relationship
                        ) + " Khmer full name"
                )
        );

        family.setFullNameEn(
                trimToNull(
                        request.fullNameEn()
                )
        );

        family.setDateOfBirth(
                request.dateOfBirth()
        );

        family.setOccupation(
                trimToNull(
                        request.occupation()
                )
        );

        family.setLifeStatus(
                request.lifeStatus()
        );

        family.setAddress(
                trimToNull(
                        request.address()
                )
        );

        memberFamilyRepository.save(
                family
        );
    }

    /*
     * ==========================================================
     * DELETE FAMILY PERSON BY RELATIONSHIP
     * ==========================================================
     */

    private void deleteFamilyByRelationship(
            Long memberId,
            FamilyRelationship relationship
    ) {
        memberFamilyRepository
                .findByMember_IdAndRelationship(
                        memberId,
                        relationship
                )
                .ifPresent(
                        memberFamilyRepository::delete
                );
    }

    /*
     * ==========================================================
     * FIND FAMILY PERSON
     * ==========================================================
     */

    private MemberFamily findFamilyByRelationship(
            Long memberId,
            FamilyRelationship relationship
    ) {
        return memberFamilyRepository
                .findByMember_IdAndRelationship(
                        memberId,
                        relationship
                )
                .orElse(null);
    }

    /*
     * ==========================================================
     * FIND MEMBER
     * ==========================================================
     */

    private Member findMember(
            Long memberId
    ) {
        if (
                memberId == null
                        || memberId <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID must be greater than zero"
            );
        }

        return memberRepository
                .findById(
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    /*
     * ==========================================================
     * BUILD RESPONSE
     * ==========================================================
     */

    private MemberFamilyInfoResponse buildResponse(
            Member member
    ) {
        Long memberId =
                member.getId();

        MemberFamily father =
                findFamilyByRelationship(
                        memberId,
                        FamilyRelationship.FATHER
                );

        MemberFamily mother =
                findFamilyByRelationship(
                        memberId,
                        FamilyRelationship.MOTHER
                );

        MemberFamily spouse =
                findFamilyByRelationship(
                        memberId,
                        FamilyRelationship.SPOUSE
                );

        return new MemberFamilyInfoResponse(
                memberId,
                member.getMaritalStatus(),
                memberFamilyMapper.toResponse(
                        father
                ),
                memberFamilyMapper.toResponse(
                        mother
                ),
                memberFamilyMapper.toResponse(
                        spouse
                )
        );
    }

    /*
     * ==========================================================
     * EMPTY SECTION CHECK
     * ==========================================================
     */

    private boolean isEmptyPerson(
            FamilyPersonRequest request
    ) {
        if (request == null) {
            return true;
        }

        return isBlank(
                request.fullNameKm()
        )
                && isBlank(
                request.fullNameEn()
        )
                && request.dateOfBirth() == null
                && isBlank(
                request.occupation()
        )
                && request.lifeStatus() == null
                && isBlank(
                request.address()
        );
    }

    /*
     * ==========================================================
     * RELATIONSHIP LABEL
     * ==========================================================
     */

    private String getRelationshipLabel(
            FamilyRelationship relationship
    ) {
        return switch (relationship) {
            case FATHER -> "Father";
            case MOTHER -> "Mother";
            case SPOUSE -> "Spouse";
        };
    }

    /*
     * ==========================================================
     * NORMALIZATION
     * ==========================================================
     */

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.isBlank();
    }
}