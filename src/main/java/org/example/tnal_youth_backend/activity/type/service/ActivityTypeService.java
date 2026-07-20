package org.example.tnal_youth_backend.activity.type.service;

import org.example.tnal_youth_backend.activity.type.dto.response.ActivityTypeResponse;

import java.util.List;

public interface ActivityTypeService {

    List<ActivityTypeResponse> getActiveActivityTypes();
}