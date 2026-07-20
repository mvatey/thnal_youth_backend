package org.example.tnal_youth_backend.activity.status.service;

import org.example.tnal_youth_backend.activity.status.dto.response.ActivityStatusResponse;

import java.util.List;

public interface ActivityStatusService {

    List<ActivityStatusResponse> getActiveActivityStatuses();
}