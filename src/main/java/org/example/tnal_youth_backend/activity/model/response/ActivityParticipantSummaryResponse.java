package org.example.tnal_youth_backend.activity.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipantSummaryResponse {

    private long total;

    private long attended;

    @JsonProperty("not_attended")
    private long notAttended;

    @JsonProperty(
            "invited_branch_participants"
    )
    private long invitedBranchParticipants;
}