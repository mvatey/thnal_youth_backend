package org.example.tnal_youth_backend.member.skill.mapper;

import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.skill.entity.MemberSkill;
import org.springframework.stereotype.Component;

@Component
public class MemberSkillMapper {

    public MemberSkillResponse toResponse(
            MemberSkill skill
    ) {
        if (skill == null) {
            return null;
        }

        return new MemberSkillResponse(
                skill.getId(),
                skill.getMember().getId(),
                skill.getSkillName(),
                skill.getProficiencyLevelId(),
                toFileResponse(
                        skill.getCertificateFile()
                ),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }

    private MemberSkillResponse.FileResponse
    toFileResponse(
            FileEntity file
    ) {
        if (file == null) {
            return null;
        }

        return new MemberSkillResponse.FileResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType(),
                file.getSizeBytes()
        );
    }
}