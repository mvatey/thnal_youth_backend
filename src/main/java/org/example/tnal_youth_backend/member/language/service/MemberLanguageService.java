package org.example.tnal_youth_backend.member.language.service;

import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;

import java.util.List;

public interface MemberLanguageService {

    List<MemberLanguageResponse> getByMemberId(
            Long memberId
    );

    MemberLanguageResponse create(
            Long memberId,
            MemberLanguageRequest request
    );

    MemberLanguageResponse update(
            Long memberId,
            Long languageId,
            MemberLanguageRequest request
    );

    void delete(
            Long memberId,
            Long languageId
    );
}