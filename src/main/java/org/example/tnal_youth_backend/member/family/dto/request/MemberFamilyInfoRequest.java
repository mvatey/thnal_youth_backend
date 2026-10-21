package org.example.tnal_youth_backend.member.family.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.example.tnal_youth_backend.member.member.entity.MaritalStatus;

public record MemberFamilyInfoRequest(

        // Null means the member prefers not to say.
        @JsonProperty("marital_status")
        MaritalStatus maritalStatus,

        @Valid
        FamilyPersonRequest father,

        @Valid
        FamilyPersonRequest mother,

        @Valid
        FamilyPersonRequest spouse
) {
}