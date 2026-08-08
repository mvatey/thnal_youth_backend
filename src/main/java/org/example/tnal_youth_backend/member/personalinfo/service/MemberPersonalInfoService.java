package org.example.tnal_youth_backend.member.personalinfo.service;

import org.example.tnal_youth_backend.member.personalinfo.dto.request.UpdateMemberPersonalInfoRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.myaccount.dto.request.UpdateMyPersonalInfoRequest;
import org.springframework.web.multipart.MultipartFile;

public interface MemberPersonalInfoService {

    MemberPersonalInfoResponse getPersonalInfo(
            Long memberId
    );

    MemberPersonalInfoResponse updatePersonalInfo(
            Long memberId,
            UpdateMemberPersonalInfoRequest request
    );

    MemberPersonalInfoResponse uploadCv(
            Long memberId,
            MultipartFile file
    );

    MemberPersonalInfoResponse updateMyPersonalInfo(
            Long memberId,
            UpdateMyPersonalInfoRequest request
    );
}