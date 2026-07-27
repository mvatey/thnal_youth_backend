package org.example.tnal_youth_backend.member.skill.service;

import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;

import java.util.List;

public interface MemberSkillService {

    List<MemberSkillResponse> getByMemberId(
            Long memberId
    );

    MemberSkillResponse create(
            Long memberId,
            MemberSkillRequest request
    );

    MemberSkillResponse update(
            Long memberId,
            Long skillId,
            MemberSkillRequest request
    );

    void delete(
            Long memberId,
            Long skillId
    );
}