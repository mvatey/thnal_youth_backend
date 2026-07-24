package org.example.tnal_youth_backend.activity.service;

import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.request.UpdateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityPageResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;

import java.time.LocalDate;

public interface ActivityService {

    ActivityResponse createActivity(
            CreateActivityRequest request,
            Long currentUserId
    );

    ActivityResponse getActivityById(
            Long activityId
    );

    ActivityPageResponse getActivities(
            int page,
            int size,
            String search,
            Short sectorId,
            Short typeId,
            LocalDate date
    );

    ActivityResponse updateActivity(
            Long activityId,
            UpdateActivityRequest request,
            Long currentUserId
    );
}