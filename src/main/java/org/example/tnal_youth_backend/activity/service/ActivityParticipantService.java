package org.example.tnal_youth_backend.activity.service;

import org.example.tnal_youth_backend.activity.model.request.InviteParticipantsRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityParticipantResponse;

import java.util.List;

public interface ActivityParticipantService {

    List<ActivityParticipantResponse> inviteParticipants(
            Long activityId,
            InviteParticipantsRequest request,
            Long currentUserId
    );

    List<ActivityParticipantResponse> getParticipants(
            Long activityId
    );

    void removeParticipant(
            Long activityId,
            Long memberId,
            Long currentUserId
    );
}