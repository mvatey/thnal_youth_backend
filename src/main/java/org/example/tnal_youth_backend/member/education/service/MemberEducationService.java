package org.example.tnal_youth_backend.member.education.service;

import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MemberEducationService {

    List<MemberEducationResponse> getByMemberId(
            Long memberId
    );

    MemberEducationResponse create(
            Long memberId,
            MemberEducationRequest request
    );

    MemberEducationResponse update(
            Long memberId,
            Long educationId,
            MemberEducationRequest request
    );

    void delete(
            Long memberId,
            Long educationId
    );

    MemberEducationResponse uploadCertificate(
            Long memberId,
            Long educationId,
            MultipartFile file
    );
}