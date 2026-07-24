package org.example.tnal_youth_backend.account.memberpoliticalaffiliation.service;

import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;

import java.util.List;

public interface MyPoliticalAffiliationService {

    List<MemberPoliticalAffiliationResponse>
    getMyPoliticalAffiliations();

    MemberPoliticalAffiliationResponse
    getMyPoliticalAffiliationById(
            Long affiliationId
    );

    MemberPoliticalAffiliationResponse
    createMyPoliticalAffiliation(
            MemberPoliticalAffiliationRequest request
    );

    MemberPoliticalAffiliationResponse
    updateMyPoliticalAffiliation(
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    );

    void deleteMyPoliticalAffiliation(
            Long affiliationId
    );
}