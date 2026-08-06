package org.example.tnal_youth_backend.member.politicalaffiliation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.entity.MemberPoliticalAffiliation;
import org.example.tnal_youth_backend.member.politicalaffiliation.mapper.MemberPoliticalAffiliationMapper;
import org.example.tnal_youth_backend.member.politicalaffiliation.repository.MemberPoliticalAffiliationRepository;
import org.example.tnal_youth_backend.member.politicalaffiliation.repository.PoliticalPartyRepository;
import org.example.tnal_youth_backend.member.politicalaffiliation.service.MemberPoliticalAffiliationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberPoliticalAffiliationServiceImpl
        implements MemberPoliticalAffiliationService {

    private final MemberPoliticalAffiliationRepository
            affiliationRepository;

    private final PoliticalPartyRepository
            politicalPartyRepository;

    private final MemberRepository
            memberRepository;

    private final MemberPoliticalAffiliationMapper
            affiliationMapper;

    private final MemberAccessValidator
            memberAccessValidator;

    @Override
    @Transactional(readOnly = true)
    public List<MemberPoliticalAffiliationResponse>
    getByMemberId(
            Long memberId
    ) {
        validateMemberAccess(memberId);

        return affiliationRepository
                .findAllByMember_IdOrderByStartDateDescIdDesc(
                        memberId
                )
                .stream()
                .map(affiliationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberPoliticalAffiliationResponse
    getById(
            Long memberId,
            Long affiliationId
    ) {
        validateMemberAccess(memberId);

        return affiliationMapper.toResponse(
                findAffiliation(
                        memberId,
                        affiliationId
                )
        );
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse create(
            Long memberId,
            MemberPoliticalAffiliationRequest request
    ) {
        validateMemberAccess(memberId);
        validateRequest(request);
        validatePartyExists(request.partyId());

        Member member =
                findMember(memberId);

        MemberPoliticalAffiliation affiliation =
                MemberPoliticalAffiliation.builder()
                        .member(member)
                        .partyId(request.partyId())
                        .country(
                                trimToNull(
                                        request.country()
                                )
                        )
                        .location(
                                trimToNull(
                                        request.location()
                                )
                        )
                        .positionTitle(
                                trimToNull(
                                        request.positionTitle()
                                )
                        )
                        .cardNo(
                                trimToNull(
                                        request.cardNo()
                                )
                        )
                        .startDate(
                                request.startDate()
                        )
                        .endDate(
                                request.endDate()
                        )
                        .isCurrent(
                                Boolean.TRUE.equals(
                                        request.isCurrent()
                                )
                        )
                        .note(
                                trimToNull(
                                        request.note()
                                )
                        )
                        .build();

        try {
            MemberPoliticalAffiliation saved =
                    affiliationRepository
                            .saveAndFlush(
                                    affiliation
                            );

            return affiliationMapper.toResponse(
                    saved
            );

        } catch (DataIntegrityViolationException exception) {
            throw affiliationConstraintException(
                    exception
            );
        }
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse update(
            Long memberId,
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    ) {
        validateMemberAccess(memberId);
        validateRequest(request);
        validatePartyExists(request.partyId());

        MemberPoliticalAffiliation affiliation =
                findAffiliation(
                        memberId,
                        affiliationId
                );

        affiliation.setPartyId(
                request.partyId()
        );

        affiliation.setCountry(
                trimToNull(
                        request.country()
                )
        );

        affiliation.setLocation(
                trimToNull(
                        request.location()
                )
        );

        affiliation.setPositionTitle(
                trimToNull(
                        request.positionTitle()
                )
        );

        affiliation.setCardNo(
                trimToNull(
                        request.cardNo()
                )
        );

        affiliation.setStartDate(
                request.startDate()
        );

        affiliation.setEndDate(
                request.endDate()
        );

        affiliation.setIsCurrent(
                Boolean.TRUE.equals(
                        request.isCurrent()
                )
        );

        affiliation.setNote(
                trimToNull(
                        request.note()
                )
        );

        try {
            MemberPoliticalAffiliation updated =
                    affiliationRepository
                            .saveAndFlush(
                                    affiliation
                            );

            return affiliationMapper.toResponse(
                    updated
            );

        } catch (DataIntegrityViolationException exception) {
            throw affiliationConstraintException(
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long affiliationId
    ) {
        validateMemberAccess(memberId);

        MemberPoliticalAffiliation affiliation =
                findAffiliation(
                        memberId,
                        affiliationId
                );

        affiliationRepository.delete(affiliation);
    }

    private void validateMemberAccess(
            Long memberId
    ) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );
    }

    private Member findMember(
            Long memberId
    ) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private MemberPoliticalAffiliation findAffiliation(
            Long memberId,
            Long affiliationId
    ) {
        if (affiliationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Political affiliation ID is required"
            );
        }

        return affiliationRepository
                .findByIdAndMember_Id(
                        affiliationId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Political affiliation not found with ID: "
                                        + affiliationId
                                        + " for member ID: "
                                        + memberId
                        )
                );
    }

    private void validatePartyExists(
            Short partyId
    ) {
        if (partyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Political party is required"
            );
        }

        boolean exists =
                politicalPartyRepository
                        .existsByIdAndIsActiveTrue(
                                partyId
                        );

        if (!exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Political party was not found or is inactive: "
                            + partyId
            );
        }
    }

    private void validateRequest(
            MemberPoliticalAffiliationRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Political affiliation request is required"
            );
        }

        if (
                request.startDate() != null
                        && request.endDate() != null
                        && request.endDate()
                        .isBefore(
                                request.startDate()
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date must be equal to or after start date"
            );
        }

        if (
                Boolean.TRUE.equals(
                        request.isCurrent()
                )
                        && request.endDate() != null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current political affiliation cannot have an end date"
            );
        }
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

    private ResponseStatusException
    affiliationConstraintException(
            DataIntegrityViolationException cause
    ) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Political affiliation could not be saved. Check that \
                party_id references an active political party and that \
                the date and current-status values are valid.
                """,
                cause
        );
    }
}