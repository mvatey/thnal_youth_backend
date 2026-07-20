package org.example.tnal_youth_backend.member.family.service;

import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyResponse;

import java.util.List;

public interface MemberFamilyService {

    List<MemberFamilyResponse> getFamilyByMemberId(
            Long memberId
    );

    MemberFamilyResponse createFamilyRecord(
            Long memberId,
            MemberFamilyRequest request
    );

    MemberFamilyResponse updateFamilyRecord(
            Long memberId,
            Long familyId,
            MemberFamilyRequest request
    );

    void deleteFamilyRecord(
            Long memberId,
            Long familyId
    );
}