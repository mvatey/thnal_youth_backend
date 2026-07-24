package org.example.tnal_youth_backend.account.membercredential.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.membercredential.service.MyCredentialService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialRequest;
import org.example.tnal_youth_backend.member.credential.dto.MemberCredentialResponse;
import org.example.tnal_youth_backend.member.credential.service.MemberCredentialService;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCredentialServiceImpl
        implements MyCredentialService {

    private final MemberCredentialService memberCredentialService;

    /*
     * Used to resolve users.member_id for the logged-in account.
     */
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<MemberCredentialResponse> getMyCredentials() {

        Long memberId = getCurrentMemberId();

        return memberCredentialService.getAllByMemberId(
                memberId
        );
    }

    @Override
    public MemberCredentialResponse getMyCredentialById(
            Long credentialId
    ) {
        Long memberId = getCurrentMemberId();

        validateCredentialId(credentialId);
        return memberCredentialService.getById(
                memberId,
                credentialId
        );
    }

    @Override
    @Transactional
    public MemberCredentialResponse createMyCredential(
            MemberCredentialRequest request
    ) {
        Long memberId = getCurrentMemberId();

        return memberCredentialService.create(
                memberId,
                request
        );
    }
    
    @Override
    @Transactional
    public MemberCredentialResponse updateMyCredential(
            Long credentialId,
            MemberCredentialRequest request
    ) {
        Long memberId = getCurrentMemberId();

        validateCredentialId(credentialId);

        return memberCredentialService.update(
                memberId,
                credentialId,
                request
        );
    }

    /*
     * ==========================================================
     * DELETE MY CREDENTIAL
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteMyCredential(
            Long credentialId
    ) {
        Long memberId = getCurrentMemberId();

        validateCredentialId(credentialId);

        memberCredentialService.delete(
                memberId,
                credentialId
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
         * Reload from the database so that memberId and
         * account information are current.
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

    /*
     * ==========================================================
     * PATH VARIABLE VALIDATION
     * ==========================================================
     */

    private void validateCredentialId(
            Long credentialId
    ) {
        if (credentialId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential ID is required"
            );
        }

        if (credentialId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credential ID must be greater than zero"
            );
        }
    }
}