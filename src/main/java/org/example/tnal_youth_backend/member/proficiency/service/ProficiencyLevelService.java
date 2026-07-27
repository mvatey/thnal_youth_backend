package org.example.tnal_youth_backend.member.proficiency.service;

import org.example.tnal_youth_backend.member.proficiency.dto.response.ProficiencyLevelResponse;

import java.util.List;

public interface ProficiencyLevelService {

    List<ProficiencyLevelResponse>
    getActiveProficiencyLevels();
}