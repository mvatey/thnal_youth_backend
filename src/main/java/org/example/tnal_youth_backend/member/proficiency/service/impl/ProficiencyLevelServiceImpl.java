package org.example.tnal_youth_backend.member.proficiency.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.proficiency.dto.response.ProficiencyLevelResponse;
import org.example.tnal_youth_backend.member.proficiency.mapper.ProficiencyLevelMapper;
import org.example.tnal_youth_backend.member.proficiency.repository.ProficiencyLevelRepository;
import org.example.tnal_youth_backend.member.proficiency.service.ProficiencyLevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProficiencyLevelServiceImpl
        implements ProficiencyLevelService {

    private final ProficiencyLevelRepository
            proficiencyLevelRepository;

    private final ProficiencyLevelMapper
            proficiencyLevelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProficiencyLevelResponse>
    getActiveProficiencyLevels() {

        return proficiencyLevelRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(proficiencyLevelMapper::toResponse)
                .toList();
    }
}