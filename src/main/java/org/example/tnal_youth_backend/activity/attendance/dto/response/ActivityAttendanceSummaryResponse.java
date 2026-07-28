package org.example.tnal_youth_backend.activity.attendance.dto.response;

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
public class ActivityAttendanceSummaryResponse {

    @JsonProperty("total_participants")
    private long totalParticipants;

    private long present;

    private long absent;

    @JsonProperty("checked_in")
    private long checkedIn;

    @JsonProperty("checked_out")
    private long checkedOut;

    @JsonProperty("not_recorded")
    private long notRecorded;
}