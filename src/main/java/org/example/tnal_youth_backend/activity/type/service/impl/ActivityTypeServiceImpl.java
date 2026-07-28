package org.example.tnal_youth_backend.activity.type.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.type.dto.response.ActivityTypeResponse;
import org.example.tnal_youth_backend.activity.type.mapper.ActivityTypeMapper;
import org.example.tnal_youth_backend.activity.type.repository.ActivityTypeRepository;
import org.example.tnal_youth_backend.activity.type.service.ActivityTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityTypeServiceImpl
        implements ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;
    private final ActivityTypeMapper activityTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityTypeResponse> getActiveActivityTypes() {

        return activityTypeRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(activityTypeMapper::toResponse)
                .toList();
    }
}