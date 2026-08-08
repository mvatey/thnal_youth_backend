package org.example.tnal_youth_backend.member.family.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.tnal_youth_backend.member.member.entity.MaritalStatus;

public record MemberFamilyInfoRequest(

        @JsonProperty("marital_status")
        @NotNull(
                message = "Marital status is required"
        )
        MaritalStatus maritalStatus,

        @Valid
        FamilyPersonRequest father,

        @Valid
        FamilyPersonRequest mother,

        @Valid
        FamilyPersonRequest spouse
) {
}