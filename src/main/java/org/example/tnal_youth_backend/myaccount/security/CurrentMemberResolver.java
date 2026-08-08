package org.example.tnal_youth_backend.myaccount.security;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CurrentMemberResolver {

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    public User getCurrentUser() {
        User principalUser =
                SecurityUtil.getCurrentUser();

        if (principalUser == null
                || principalUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository
                .findById(
                        principalUser.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }

    public Long getCurrentMemberId() {
        User currentUser =
                getCurrentUser();

        Long memberId =
                currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This account is not linked to a member profile"
            );
        }

        return memberId;
    }

    public Member getCurrentMember() {
        Long memberId =
                getCurrentMemberId();

        return memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Linked member profile was not found with ID: "
                                        + memberId
                        )
                );
    }
}