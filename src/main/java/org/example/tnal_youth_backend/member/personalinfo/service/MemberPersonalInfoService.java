package org.example.tnal_youth_backend.member.personalinfo.service;

import org.example.tnal_youth_backend.member.personalinfo.dto.request.UpdateMemberPersonalInfoRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;

public interface MemberPersonalInfoService {

    MemberPersonalInfoResponse getPersonalInfo(
            Long memberId
    );

    MemberPersonalInfoResponse updatePersonalInfo(
            Long memberId,
            UpdateMemberPersonalInfoRequest request
    );
}