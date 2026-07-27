package org.example.tnal_youth_backend.member.member.service;

import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberSummaryResponse;

import java.util.List;

public interface MemberService {

    List<MemberListResponse> getAllMembers();

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
}