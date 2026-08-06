package org.example.tnal_youth_backend.member.language.service;

import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.springframework.web.multipart.MultipartFile;

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

    MemberLanguageResponse uploadCertificate(
            Long memberId,
            Long skillId,
            MultipartFile file
    );

    MemberLanguageResponse removeCertificate(
            Long memberId,
            Long skillId
    );
}