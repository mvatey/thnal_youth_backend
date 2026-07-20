package org.example.tnal_youth_backend.activity.activity.service;

import org.example.tnal_youth_backend.activity.activity.dto.request.ActivityRequest;
import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityResponse;

import java.util.List;

public interface ActivityService {

    List<ActivityResponse> getAllActivities();

    ActivityResponse getActivityById(Long id);

    ActivityResponse createActivity(
            ActivityRequest request
    );

    ActivityResponse updateActivity(
            Long id,
            ActivityRequest request
    );

    void deleteActivity(Long id);
}