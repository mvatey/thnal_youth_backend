package org.example.tnal_youth_backend.account.myaccount.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MyAccountSummaryResponse(

        @JsonProperty("attended_activities")
        long attendedActivities,

        @JsonProperty("absent_activities")
        long absentActivities,

        @JsonProperty("total_donations")
        long totalDonations
) {
}