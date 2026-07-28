package org.example.tnal_youth_backend.member.member.service;

import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberStatusRequest;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberPageResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberSummaryResponse;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.util.List;

public interface MemberService {

    MemberPageResponse getMembers(
            int page,
            int size,
            String search,
            Long branchId,
            Short statusId,
            Gender gender
    );

    MemberSummaryResponse getMemberSummary();

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
