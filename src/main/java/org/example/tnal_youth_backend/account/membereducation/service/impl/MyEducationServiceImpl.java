package org.example.tnal_youth_backend.account.membereducation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.membereducation.service.MyEducationService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.education.entity.MemberEducation;
import org.example.tnal_youth_backend.member.education.mapper.MemberEducationMapper;
import org.example.tnal_youth_backend.member.education.repository.MemberEducationRepository;
import org.example.tnal_youth_backend.member.education.service.MemberEducationService;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyEducationServiceImpl
        implements MyEducationService {

    private final MemberEducationService memberEducationService;

    private final MemberEducationRepository memberEducationRepository;

    private final MemberEducationMapper memberEducationMapper;

    /*
     * Used to resolve the logged-in user and linked member.
     */
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<MemberEducationResponse> getMyEducation() {

        Long memberId = getCurrentMemberId();

        /*
         * Delegate to the existing Member Education service.
         */
        return memberEducationService.getByMemberId(
                memberId
        );
    }

    @Override
    public MemberEducationResponse getMyEducationById(
            Long educationId
    ) {
        Long memberId = getCurrentMemberId();

        MemberEducation education =
                findOwnedEducation(
                        memberId,
                        educationId
                );

        return memberEducationMapper.toResponse(
                education
        );
    }

    @Override
    @Transactional
    public MemberEducationResponse createMyEducation(
            MemberEducationRequest request
    ) {
        Long memberId = getCurrentMemberId();

        return memberEducationService.create(
                memberId,
                request
        );
    }

    @Override
    @Transactional
    public MemberEducationResponse updateMyEducation(
            Long educationId,
            MemberEducationRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The existing service already uses:
         *
         * findByIdAndMemberId(educationId, memberId)
         *
         * Therefore, a member cannot update another
         * member's education record.
         */
        return memberEducationService.update(
                memberId,
                educationId,
                request
        );
    }

    @Override
    @Transactional
    public void deleteMyEducation(
            Long educationId
    ) {
        Long memberId = getCurrentMemberId();

        memberEducationService.delete(
                memberId,
                educationId
        );
    }

    /*
     * ==========================================================
     * FIND OWNED EDUCATION
     * ==========================================================
     */

    private MemberEducation findOwnedEducation(
            Long memberId,
            Long educationId
    ) {
        if (educationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Education ID is required"
            );
        }

        return memberEducationRepository
                .findByIdAndMemberId(
                        educationId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Education record was not found"
                        )
                );
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED MEMBER
     * ==========================================================
     */

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

        /*
         * Reload the user from the database so memberId,
         * status, and other account information are current.
         */
        User currentUser =
                userRepository
                        .findById(
                                authenticatedUser.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found "
                                                + "in the database"
                                )
                        );

        if (currentUser.getMemberId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to "
                            + "a member profile"
            );
        }

        /*
         * Verify that the linked member still exists.
         */
        if (!memberRepository.existsById(
                currentUser.getMemberId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to "
                            + "this account was not found"
            );
        }

        return currentUser.getMemberId();
    }
}