package org.example.tnal_youth_backend.activity.attendance.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceMemberRequest {

    @NotNull(message = "Member ID is required")
    @JsonProperty("member_id")
    private Long memberId;
}