package org.example.tnal_youth_backend.member.proficiency.mapper;

import org.example.tnal_youth_backend.member.proficiency.dto.response.ProficiencyLevelResponse;
import org.example.tnal_youth_backend.member.proficiency.entity.ProficiencyLevel;
import org.springframework.stereotype.Component;

@Component
public class ProficiencyLevelMapper {

    public ProficiencyLevelResponse toResponse(
            ProficiencyLevel level
    ) {
        if (level == null) {
            return null;
        }

        return new ProficiencyLevelResponse(
                level.getId(),
                level.getCode(),
                level.getLabelKm(),
                level.getLabelEn(),
                level.getDescription(),
                level.getSortOrder()
        );
    }
}