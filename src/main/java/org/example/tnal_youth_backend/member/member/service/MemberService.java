package org.example.tnal_youth_backend.member.member.service;

import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberProfilePhotoRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberStatusRequest;
import org.example.tnal_youth_backend.member.member.dto.response.*;
import org.example.tnal_youth_backend.member.member.entity.Gender;

public interface MemberService {

    MemberPageResponse getMembers(
            int page,
            int size,
            String search,
            Long branchId,
            Short statusId,
            Gender gender
    );

    MemberDetailResponse updateMemberProfilePhoto(
            Long memberId,
            UpdateMemberProfilePhotoRequest request
    );

    MemberSummaryResponse getMemberSummary();

    MemberDetailSummaryResponse getMemberDetailSummary(
            Long memberId
    );

    MemberMonthlyDonationSummaryResponse
    getMemberMonthlyDonationSummary(
            Long memberId
    );



    MemberDetailResponse getMemberById(
            Long id
    );

    MemberDetailResponse createMember(
            CreateMemberRequest request
    );

    MemberDetailResponse updateMember(
            Long id,
            UpdateMemberRequest request
    );

    void deleteMember(
            Long id
    );

    MemberDetailResponse updateMemberStatus(
            Long id,
            UpdateMemberStatusRequest request
    );
}