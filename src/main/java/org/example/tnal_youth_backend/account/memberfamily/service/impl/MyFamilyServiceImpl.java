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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyFamilyServiceImpl
        implements MyFamilyService {

    private final MemberFamilyRepository memberFamilyRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MyFamilyMapper myFamilyMapper;

    @Override
    public List<MyFamilyResponse> getMyFamily() {

        Member currentMember = getCurrentMember();

        return memberFamilyRepository
                .findAllByMemberIdOrderByIdAsc(
                        currentMember.getId()
                )
                .stream()
                .map(myFamilyMapper::toResponse)
                .toList();
    }

    @Override
    public MyFamilyResponse getMyFamilyMember(
            Long familyId
    ) {
        Member currentMember = getCurrentMember();

        MemberFamily family =
                getOwnedFamilyRecord(
                        familyId,
                        currentMember.getId()
                );

        return myFamilyMapper.toResponse(family);
    }

    @Override
    @Transactional
    public MyFamilyResponse createMyFamilyMember(
            MyFamilyRequest request
    ) {
        Member currentMember = getCurrentMember();

        MemberFamily family = new MemberFamily();

        family.setMember(currentMember);

        myFamilyMapper.updateEntity(
                family,
                request
        );

        MemberFamily savedFamily =
                memberFamilyRepository.save(family);

        return myFamilyMapper.toResponse(
                savedFamily
        );
    }

    @Override
    @Transactional
    public MyFamilyResponse updateMyFamilyMember(
            Long familyId,
            MyFamilyRequest request
    ) {
        Member currentMember = getCurrentMember();

        MemberFamily family =
                getOwnedFamilyRecord(
                        familyId,
                        currentMember.getId()
                );

        myFamilyMapper.updateEntity(
                family,
                request
        );

        MemberFamily savedFamily =
                memberFamilyRepository.save(family);

        return myFamilyMapper.toResponse(
                savedFamily
        );
    }

    @Override
    @Transactional
    public void deleteMyFamilyMember(
            Long familyId
    ) {
        Member currentMember = getCurrentMember();

        MemberFamily family =
                getOwnedFamilyRecord(
                        familyId,
                        currentMember.getId()
                );

        memberFamilyRepository.delete(family);
    }

    private Member getCurrentMember() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {
            throw new IllegalStateException(
                    "Authenticated user was not found"
            );
        }

        User currentUser =
                userRepository
                        .findById(
                                authenticatedUser.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated user was not found"
                                )
                        );

        if (currentUser.getMemberId() == null) {
            throw new IllegalStateException(
                    "This account is not linked to a member profile"
            );
        }

        return memberRepository
                .findById(
                        currentUser.getMemberId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Linked member profile was not found"
                        )
                );
    }

    private MemberFamily getOwnedFamilyRecord(
            Long familyId,
            Long memberId
    ) {
        if (familyId == null) {
            throw new IllegalArgumentException(
                    "Family ID is required"
            );
        }

        return memberFamilyRepository
                .findByIdAndMemberId(
                        familyId,
                        memberId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Family record was not found"
                        )
                );
    }
}