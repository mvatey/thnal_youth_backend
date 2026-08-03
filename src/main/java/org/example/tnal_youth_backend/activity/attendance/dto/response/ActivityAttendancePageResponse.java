package org.example.tnal_youth_backend.activity.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAttendancePageResponse {

    private List<ActivityAttendanceResponse> attendance;

    private ActivityAttendanceSummaryResponse summary;
}