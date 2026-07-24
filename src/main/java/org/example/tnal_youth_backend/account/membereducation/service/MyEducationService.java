package org.example.tnal_youth_backend.account.membereducation.service;

import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;

import java.util.List;

public interface MyEducationService {

    List<MemberEducationResponse> getMyEducation();

    MemberEducationResponse getMyEducationById(
            Long educationId
    );

    MemberEducationResponse createMyEducation(
            MemberEducationRequest request
    );

    MemberEducationResponse updateMyEducation(
            Long educationId,
            MemberEducationRequest request
    );

    void deleteMyEducation(
            Long educationId
    );
}