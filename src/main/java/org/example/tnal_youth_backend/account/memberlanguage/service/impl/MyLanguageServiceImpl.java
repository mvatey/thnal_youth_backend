package org.example.tnal_youth_backend.account.memberlanguage.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberlanguage.service.MyLanguageService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.language.entity.MemberLanguage;
import org.example.tnal_youth_backend.member.language.mapper.MemberLanguageMapper;
import org.example.tnal_youth_backend.member.language.repository.MemberLanguageRepository;
import org.example.tnal_youth_backend.member.language.service.MemberLanguageService;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyLanguageServiceImpl
        implements MyLanguageService {

    /*
     * Reuse the existing Member Language service.
     *
     * It already handles:
     *
     * - member validation
     * - duplicate language validation
     * - language name normalization
     * - proficiency-level foreign-key validation
     * - ownership checking for update and delete
     */
    private final MemberLanguageService memberLanguageService;

    /*
     * Used only for reading one language record that belongs
     * to the logged-in member.
     */
    private final MemberLanguageRepository memberLanguageRepository;
    private final MemberLanguageMapper memberLanguageMapper;

    /*
     * Used to resolve the authenticated user's linked member.
     */
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    //  GET ALL MY LANGUAGES

    @Override
    public List<MemberLanguageResponse> getMyLanguages() {

        Long memberId = getCurrentMemberId();

        return memberLanguageService.getByMemberId(
                memberId
        );
    }

    /*
     * GET ONE MY LANGUAGE
     */

    @Override
    public MemberLanguageResponse getMyLanguageById(
            Long languageId
    ) {
        Long memberId = getCurrentMemberId();

        MemberLanguage language =
                findOwnedLanguage(
                        memberId,
                        languageId
                );

        return memberLanguageMapper.toResponse(
                language
        );
    }

    /*
     * CREATE MY LANGUAGE
     */

    @Override
    @Transactional
    public MemberLanguageResponse createMyLanguage(
            MemberLanguageRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * memberId is not sent by the frontend.
         *
         * It is taken from users.member_id for the
         * currently authenticated user.
         */
        return memberLanguageService.create(
                memberId,
                request
        );
    }

    //    UPDATE

    @Override
    @Transactional
    public MemberLanguageResponse updateMyLanguage(
            Long languageId,
            MemberLanguageRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The existing service finds the language using:
         *
         * findByIdAndMemberId(languageId, memberId)
         *
         * This prevents members from updating another
         * member's language record.
         */
        return memberLanguageService.update(
                memberId,
                languageId,
                request
        );
    }


    // Delete
    @Override
    @Transactional
    public void deleteMyLanguage(
            Long languageId
    ) {
        Long memberId = getCurrentMemberId();

        memberLanguageService.delete(
                memberId,
                languageId
        );
    }

    /*
     * ==========================================================
     * FIND OWNED LANGUAGE
     * ==========================================================
     */

    private MemberLanguage findOwnedLanguage(
            Long memberId,
            Long languageId
    ) {
        if (languageId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Language ID is required"
            );
        }

        return memberLanguageRepository
                .findByIdAndMemberId(
                        languageId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Language record was not found"
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
}