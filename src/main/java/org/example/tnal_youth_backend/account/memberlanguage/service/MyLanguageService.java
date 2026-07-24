package org.example.tnal_youth_backend.account.memberlanguage.service;

import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;

import java.util.List;

public interface MyLanguageService {

    /*
     * Get all language records belonging to
     * the currently logged-in member.
     */
    List<MemberLanguageResponse> getMyLanguages();

    /*
     * Get one language record belonging to
     * the currently logged-in member.
     */
    MemberLanguageResponse getMyLanguageById(
            Long languageId
    );

    /*
     * Create a language record for
     * the currently logged-in member.
     */
    MemberLanguageResponse createMyLanguage(
            MemberLanguageRequest request
    );

    /*
     * Update a language record belonging to
     * the currently logged-in member.
     */
    MemberLanguageResponse updateMyLanguage(
            Long languageId,
            MemberLanguageRequest request
    );

    /*
     * Delete a language record belonging to
     * the currently logged-in member.
     */
    void deleteMyLanguage(
            Long languageId
    );
}