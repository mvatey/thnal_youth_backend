package org.example.tnal_youth_backend.member.personalinfo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AssignMemberBranchRequest(

        @NotNull
        @JsonProperty("branch_id")
        Long branchId
) {
}
