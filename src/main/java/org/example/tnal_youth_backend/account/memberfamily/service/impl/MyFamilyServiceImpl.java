package org.example.tnal_youth_backend.account.memberfamily.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberfamily.dto.request.MyFamilyRequest;
import org.example.tnal_youth_backend.account.memberfamily.dto.response.MyFamilyResponse;
import org.example.tnal_youth_backend.account.memberfamily.mapper.MyFamilyMapper;
import org.example.tnal_youth_backend.account.memberfamily.service.MyFamilyService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.example.tnal_youth_backend.member.family.repository.MemberFamilyRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyFamilyServiceImpl
        implements MyFamilyService {

    private final MemberFamilyRepository
            memberFamilyRepository;

    private final MemberRepository
            memberRepository;

    private final UserRepository
            userRepository;

    private final MyFamilyMapper
            myFamilyMapper;

    /*
     * ==========================================================
     * GET ALL FAMILY RECORDS FOR CURRENT MEMBER
     * ==========================================================
     */

    @Override
    public List<MyFamilyResponse> getMyFamily() {

        Member currentMember =
                getCurrentMember();

        return memberFamilyRepository
                .findAllByMember_IdOrderByIdAsc(
                        currentMember.getId()
                )
                .stream()
                .map(
                        myFamilyMapper::toResponse
                )
                .toList();
    }

    /*
     * ==========================================================
     * GET ONE FAMILY RECORD
     * ==========================================================
     */

    @Override
    public MyFamilyResponse getMyFamilyMember(
            Long familyId
    ) {
        Member currentMember =
                getCurrentMember();

        MemberFamily family =
                getOwnedFamilyRecord(
                        familyId,
                        currentMember.getId()
                );

        return myFamilyMapper.toResponse(
                family
        );
    }

    /*
     * ==========================================================
     * CREATE FAMILY RECORD
     * ==========================================================
     */

    @Override
    @Transactional
    public MyFamilyResponse createMyFamilyMember(
            MyFamilyRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family request is required"
            );
        }

        Member currentMember =
                getCurrentMember();

        validateRelationshipIsNotDuplicate(
                currentMember.getId(),
                request
        );

        MemberFamily family =
                new MemberFamily();

        family.setMember(
                currentMember
        );

        myFamilyMapper.updateEntity(
                family,
                request
        );

        MemberFamily savedFamily =
                memberFamilyRepository
                        .saveAndFlush(
                                family
                        );

        return myFamilyMapper.toResponse(
                savedFamily
        );
    }

    /*
     * ==========================================================
     * UPDATE FAMILY RECORD
     * ==========================================================
     */

    @Override
    @Transactional
    public MyFamilyResponse updateMyFamilyMember(
            Long familyId,
            MyFamilyRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family request is required"
            );
        }

        Member currentMember =
                getCurrentMember();

        MemberFamily family =
                getOwnedFamilyRecord(
                        familyId,
                        currentMember.getId()
                );

        validateRelationshipIsNotUsedByAnotherRecord(
                currentMember.getId(),
                familyId,
                request
        );

        myFamilyMapper.updateEntity(
                family,
                request
        );

        MemberFamily savedFamily =
                memberFamilyRepository
                        .saveAndFlush(
                                family
                        );

        return myFamilyMapper.toResponse(
                savedFamily
        );
    }

    /*
     * ==========================================================
     * DELETE FAMILY RECORD
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteMyFamilyMember(
            Long familyId
    ) {
        Member currentMember =
                getCurrentMember();

        MemberFamily family =
                getOwnedFamilyRecord(
                        familyId,
                        currentMember.getId()
                );

        memberFamilyRepository.delete(
                family
        );

        memberFamilyRepository.flush();
    }

    /*
     * ==========================================================
     * CURRENT MEMBER
     * ==========================================================
     */

    private Member getCurrentMember() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (
                authenticatedUser == null
                        || authenticatedUser.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(
                                authenticatedUser.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        if (currentUser.getMemberId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is not linked to a member profile"
            );
        }

        return memberRepository
                .findById(
                        currentUser.getMemberId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Linked member profile was not found"
                        )
                );
    }

    /*
     * ==========================================================
     * OWNED FAMILY RECORD
     * ==========================================================
     */

    private MemberFamily getOwnedFamilyRecord(
            Long familyId,
            Long memberId
    ) {
        if (
                familyId == null
                        || familyId <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family ID must be greater than zero"
            );
        }

        return memberFamilyRepository
                .findByIdAndMember_Id(
                        familyId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Family record was not found"
                        )
                );
    }

    /*
     * ==========================================================
     * DUPLICATE RELATIONSHIP VALIDATION
     * ==========================================================
     */

    private void validateRelationshipIsNotDuplicate(
            Long memberId,
            MyFamilyRequest request
    ) {
        if (request.relationship() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family relationship is required"
            );
        }

        boolean relationshipExists =
                memberFamilyRepository
                        .existsByMember_IdAndRelationship(
                                memberId,
                                request.relationship()
                        );

        if (relationshipExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A family record already exists for relationship: "
                            + request.relationship()
            );
        }
    }

    private void validateRelationshipIsNotUsedByAnotherRecord(
            Long memberId,
            Long currentFamilyId,
            MyFamilyRequest request
    ) {
        if (request.relationship() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Family relationship is required"
            );
        }

        boolean relationshipExists =
                memberFamilyRepository
                        .existsByMember_IdAndRelationshipAndIdNot(
                                memberId,
                                request.relationship(),
                                currentFamilyId
                        );

        if (relationshipExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another family record already uses relationship: "
                            + request.relationship()
            );
        }
    }
}