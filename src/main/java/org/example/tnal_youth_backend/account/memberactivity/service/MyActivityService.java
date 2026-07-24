package org.example.tnal_youth_backend.account.memberactivity.service;

import org.example.tnal_youth_backend.account.memberactivity.dto.response.MyActivityResponse;
import org.example.tnal_youth_backend.account.memberactivity.dto.response.MyActivitySummaryResponse;

import java.util.List;

public interface MyActivityService {

    List<MyActivityResponse> getMyActivities();

    MyActivityResponse getMyActivityById(
            Long activityId
    );

    MyActivitySummaryResponse getMyActivitySummary();
}