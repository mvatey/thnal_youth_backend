package org.example.tnal_youth_backend.member.politicalaffiliation.service;

import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;

import java.util.List;

public interface MemberPoliticalAffiliationService {

    List<MemberPoliticalAffiliationResponse> getByMemberId(
            Long memberId
    );

    MemberPoliticalAffiliationResponse getById(
            Long memberId,
            Long affiliationId
    );

    MemberPoliticalAffiliationResponse create(
            Long memberId,
            MemberPoliticalAffiliationRequest request
    );

    MemberPoliticalAffiliationResponse update(
            Long memberId,
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    );

    void delete(
            Long memberId,
            Long affiliationId
    );
}