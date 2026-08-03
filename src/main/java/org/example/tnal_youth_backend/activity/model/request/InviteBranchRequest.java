package org.example.tnal_youth_backend.activity.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InviteBranchRequest {

    @NotNull(message = "Branch ID is required")
    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("can_manage_attendance")
    private Boolean canManageAttendance = false;

    @JsonProperty("can_record_donation")
    private Boolean canRecordDonation = false;

    @Size(
            max = 1000,
            message = "Invitation note must not exceed 1000 characters"
    )
    private String note;
}