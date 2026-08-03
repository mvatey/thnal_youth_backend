package org.example.tnal_youth_backend.activity.attendance.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAttendanceResponse {

    @JsonProperty("participant_id")
    private Long participantId;

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("member_no")
    private String memberNo;

    @JsonProperty("full_name_km")
    private String fullNameKm;

    @JsonProperty("full_name_en")
    private String fullNameEn;

    private String phone;

    private String email;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("attendance_status_id")
    private Short attendanceStatusId;

    @JsonProperty("attendance_status")
    private String attendanceStatus;

    @JsonProperty("checked_in_at")
    private OffsetDateTime checkedInAt;

    @JsonProperty("checked_out_at")
    private OffsetDateTime checkedOutAt;

    @JsonProperty("registered_at")
    private OffsetDateTime registeredAt;
}