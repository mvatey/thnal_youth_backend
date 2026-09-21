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
            Long activityId,
            Long branchId,
            Long currentUserId
    );

    ActivityPageResponse getActivities(
            int page,
            int size,
            String search,
            Short sectorId,
            Short typeId,
            LocalDate date,
            Long branchId,
            Long currentUserId
    );

    ActivityResponse updateActivity(
            Long activityId,
            UpdateActivityRequest request,
            Long currentUserId
    );

    ActivityResponse completeActivity(
            Long activityId,
            Long currentUserId
    );

    /**
     * Permanently removes the activity and every piece of data tied to it
     * (participation, attendance, expenses, invited branches, daily
     * schedules, activity-scoped donations, and certificates issued for
     * it). Notifies every current participant and every invited branch's
     * staff that it's been cancelled before anything is deleted. Only the
     * host branch's own Secretary/Branch Leader staff may do this -- see
     * ActivityServiceImpl.validateUpdatePermission.
     */
    void deleteActivity(
            Long activityId,
            Long currentUserId
    );

}
