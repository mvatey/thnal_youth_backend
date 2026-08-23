package org.example.tnal_youth_backend.member.personalinfo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record AssignMemberBranchesRequest(

        @NotEmpty(message = "At least one branch is required")
        @Valid
        @JsonProperty("branch_ids")
        List<
                @NotNull(message = "Branch ID is required")
                @Positive(message = "Branch ID must be greater than zero")
                Long
                > branchIds
) {
}
