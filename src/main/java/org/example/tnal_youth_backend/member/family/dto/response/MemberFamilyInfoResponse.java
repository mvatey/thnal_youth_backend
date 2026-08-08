package org.example.tnal_youth_backend.member.family.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.member.member.entity.MaritalStatus;

public record MemberFamilyInfoResponse(

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("marital_status")
        MaritalStatus maritalStatus,

        FamilyPersonResponse father,

        FamilyPersonResponse mother,

        FamilyPersonResponse spouse
) {
}

