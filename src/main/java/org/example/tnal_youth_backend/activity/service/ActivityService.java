package org.example.tnal_youth_backend.activity.service;

import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;

public interface ActivityService {

    ActivityResponse createActivity(
            CreateActivityRequest request,
            Long currentUserId
    );
}