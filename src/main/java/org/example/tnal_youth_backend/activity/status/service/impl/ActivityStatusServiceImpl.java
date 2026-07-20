package org.example.tnal_youth_backend.activity.status.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.status.dto.response.ActivityStatusResponse;
import org.example.tnal_youth_backend.activity.status.mapper.ActivityStatusMapper;
import org.example.tnal_youth_backend.activity.status.repository.ActivityStatusRepository;
import org.example.tnal_youth_backend.activity.status.service.ActivityStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityStatusServiceImpl
        implements ActivityStatusService {

    private final ActivityStatusRepository
            activityStatusRepository;

    private final ActivityStatusMapper
            activityStatusMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityStatusResponse>
    getActiveActivityStatuses() {

        return activityStatusRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(activityStatusMapper::toResponse)
                .toList();
    }
}