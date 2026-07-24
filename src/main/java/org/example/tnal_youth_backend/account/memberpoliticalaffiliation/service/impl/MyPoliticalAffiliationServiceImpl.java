package org.example.tnal_youth_backend.account.memberpoliticalaffiliation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberpoliticalaffiliation.service.MyPoliticalAffiliationService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.service.MemberPoliticalAffiliationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPoliticalAffiliationServiceImpl
        implements MyPoliticalAffiliationService {

    private final MemberPoliticalAffiliationService
            affiliationService;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<MemberPoliticalAffiliationResponse>
    getMyPoliticalAffiliations() {

        return affiliationService.getByMemberId(
                getCurrentMemberId()
        );
    }

    @Override
    public MemberPoliticalAffiliationResponse
    getMyPoliticalAffiliationById(
            Long affiliationId
    ) {
        return affiliationService.getById(
                getCurrentMemberId(),
                affiliationId
        );
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse
    createMyPoliticalAffiliation(
            MemberPoliticalAffiliationRequest request
    ) {
        return affiliationService.create(
                getCurrentMemberId(),
                request
        );
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse
    updateMyPoliticalAffiliation(
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    ) {
        return affiliationService.update(
                getCurrentMemberId(),
                affiliationId,
                request
        );
    }

    @Override
    @Transactional
    public void deleteMyPoliticalAffiliation(
            Long affiliationId
    ) {
        affiliationService.delete(
                getCurrentMemberId(),
                affiliationId
        );
    }

    private Long getCurrentMemberId() {
        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        User currentUser =
                userRepository
                        .findById(authenticatedUser.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found "
                                                + "in the database"
                                )
                        );

        Long memberId = currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to "
                            + "a member profile"
            );
        }

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to "
                            + "this account was not found"
            );
        }

        return memberId;
    }
}