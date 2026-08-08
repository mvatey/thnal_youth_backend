package org.example.tnal_youth_backend.member.family.service;

import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyInfoRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyInfoResponse;

public interface MemberFamilyService {

    MemberFamilyInfoResponse getFamilyInfo(
            Long memberId
    );

    MemberFamilyInfoResponse updateFamilyInfo(
            Long memberId,
            MemberFamilyInfoRequest request
    );
}