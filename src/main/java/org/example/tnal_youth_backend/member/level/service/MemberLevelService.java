package org.example.tnal_youth_backend.member.level.service;

import org.example.tnal_youth_backend.member.level.dto.MemberLevelRequest;
import org.example.tnal_youth_backend.member.level.dto.MemberLevelResponse;

import java.util.List;

public interface MemberLevelService {

    List<MemberLevelResponse> getAllMemberLevels(
            Boolean activeOnly
    );

    MemberLevelResponse getMemberLevelById(
            Short id
    );

    MemberLevelResponse getMemberLevelByCode(
            String code
    );

    MemberLevelResponse createMemberLevel(
            MemberLevelRequest request
    );

    MemberLevelResponse updateMemberLevel(
            Short id,
            MemberLevelRequest request
    );

    void deleteMemberLevel(
            Short id
    );
}