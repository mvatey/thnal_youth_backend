package org.example.tnal_youth_backend.member.status.service;

import org.example.tnal_youth_backend.member.status.dto.MemberStatusRequest;
import org.example.tnal_youth_backend.member.status.dto.MemberStatusResponse;

import java.util.List;

public interface MemberStatusService {

    List<MemberStatusResponse> getAllMemberStatuses(
            Boolean activeOnly
    );

    MemberStatusResponse getMemberStatusById(
            Short id
    );

    MemberStatusResponse getMemberStatusByCode(
            String code
    );

    MemberStatusResponse createMemberStatus(
            MemberStatusRequest request
    );

    MemberStatusResponse updateMemberStatus(
            Short id,
            MemberStatusRequest request
    );

    void deleteMemberStatus(
            Short id
    );
}