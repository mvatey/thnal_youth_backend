package org.example.tnal_youth_backend.account.memberactivity.dto.response;

public record MyActivitySummaryResponse(

        long totalActivities,

        long invitedActivities,

        long attendedActivities,

        long absentActivities,

        long pendingActivities
) {
}