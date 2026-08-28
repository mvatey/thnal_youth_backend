package org.example.tnal_youth_backend.activity.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipantResponse {

    private Long id;

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

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("branch_name_km")
    private String branchNameKm;

    @JsonProperty("branch_name_en")
    private String branchNameEn;

    @JsonProperty("attendance_status_id")
    private Short attendanceStatusId;

    /** PRESENT or ABSENT, returned so every attendance view uses one source. */
    @JsonProperty("attendance_status")
    private String attendanceStatus;

    @JsonProperty("registration_source")
    private ParticipantRegistrationSource registrationSource;

    @JsonProperty("invited_branch_id")
    private Long invitedBranchId;

    @JsonProperty("invited_by")
    private Long invitedBy;

    @JsonProperty("registered_at")
    private OffsetDateTime registeredAt;

    @JsonProperty("checked_in_at")
    private OffsetDateTime checkedInAt;

    @JsonProperty("checked_out_at")
    private OffsetDateTime checkedOutAt;

    private String note;
}
