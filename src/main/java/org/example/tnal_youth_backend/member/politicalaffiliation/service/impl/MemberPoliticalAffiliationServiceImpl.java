package org.example.tnal_youth_backend.member.politicalaffiliation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.entity.MemberPoliticalAffiliation;
import org.example.tnal_youth_backend.member.politicalaffiliation.mapper.MemberPoliticalAffiliationMapper;
import org.example.tnal_youth_backend.member.politicalaffiliation.repository.MemberPoliticalAffiliationRepository;
import org.example.tnal_youth_backend.member.politicalaffiliation.service.MemberPoliticalAffiliationService;
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

    private final MemberRepository memberRepository;

    private final MemberPoliticalAffiliationMapper
            affiliationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberPoliticalAffiliationResponse>
    getByMemberId(Long memberId) {

        verifyMemberExists(memberId);

        return affiliationRepository
                .findAllByMemberIdOrderByIdAsc(memberId)
                .stream()
                .map(affiliationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberPoliticalAffiliationResponse getById(
            Long memberId,
            Long affiliationId
    ) {
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
        Member member = findMember(memberId);

        validateRequest(request);

        MemberPoliticalAffiliation affiliation =
                MemberPoliticalAffiliation.builder()
                        .member(member)
                        .build();

        affiliationMapper.updateEntity(
                affiliation,
                request
        );

        MemberPoliticalAffiliation saved =
                affiliationRepository.saveAndFlush(
                        affiliation
                );

        return affiliationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse update(
            Long memberId,
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    ) {
        validateRequest(request);

        MemberPoliticalAffiliation affiliation =
                findAffiliation(
                        memberId,
                        affiliationId
                );

        affiliationMapper.updateEntity(
                affiliation,
                request
        );

        MemberPoliticalAffiliation updated =
                affiliationRepository.saveAndFlush(
                        affiliation
                );

        return affiliationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long affiliationId
    ) {
        MemberPoliticalAffiliation affiliation =
                findAffiliation(
                        memberId,
                        affiliationId
                );

        affiliationRepository.delete(affiliation);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

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

    private void verifyMemberExists(Long memberId) {
        findMember(memberId);
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
                .findByIdAndMemberId(
                        affiliationId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Political affiliation not found "
                                        + "with ID: "
                                        + affiliationId
                                        + " for member ID: "
                                        + memberId
                        )
                );
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

        if (request.affiliationName() == null
                || request.affiliationName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Affiliation name is required"
            );
        }

        if (request.startDate() != null
                && request.endDate() != null
                && request.endDate()
                .isBefore(request.startDate())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date must be equal to "
                            + "or after start date"
            );
        }
    }
}