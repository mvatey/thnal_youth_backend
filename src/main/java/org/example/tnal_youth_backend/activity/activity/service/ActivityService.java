//package org.example.tnal_youth_backend.activity.activity.service;
//
//import org.example.tnal_youth_backend.activity.activity.dto.request.ActivityRequest;
//import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityListResponse;
//import org.example.tnal_youth_backend.activity.activity.dto.response.ActivityResponse;
//
//import java.util.List;
//
//public interface ActivityService {
//
//    /*
//     * Small response used by the Activity table.
//     */
//    List<ActivityListResponse> getAllActivities();
//
//    /*
//     * Search the Activity table by Khmer or English title.
//     */
//    List<ActivityListResponse> searchActivities(
//            String search
//    );
//
//    /*
//     * Filter the Activity table by activity type.
//     */
//    List<ActivityListResponse> filterActivitiesByType(
//            Short typeId
//    );
//
//    /*
//     * Full response used by the Activity detail page.
//     */
//    ActivityResponse getActivityById(
//            Long id
//    );
//
//    /*
//     * Create an activity and return its full response.
//     */
//    ActivityResponse createActivity(
//            ActivityRequest request
//    );
//
//    /*
//     * Update an activity and return its full response.
//     */
//    ActivityResponse updateActivity(
//            Long id,
//            ActivityRequest request
//    );
//
//    /*
//     * Delete an activity.
//     */
//    void deleteActivity(
//            Long id
//    );
//}