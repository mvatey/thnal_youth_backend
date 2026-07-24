package org.example.tnal_youth_backend.account.memberskill.service;

import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;

import java.util.List;

public interface MySkillService {

    List<MemberSkillResponse> getMySkills();

    MemberSkillResponse getMySkillById(
            Long skillId
    );

    MemberSkillResponse createMySkill(
            MemberSkillRequest request
    );

    MemberSkillResponse updateMySkill(
            Long skillId,
            MemberSkillRequest request
    );

    void deleteMySkill(
            Long skillId
    );
}