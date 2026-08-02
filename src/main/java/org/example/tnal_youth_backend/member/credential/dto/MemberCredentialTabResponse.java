package org.example.tnal_youth_backend.member.credential.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MemberCredentialTabResponse(

        @JsonProperty("membership_card")
        MemberCredentialResponse membershipCard,

        @JsonProperty("certificates")
        List<MemberCredentialResponse> certificates,

        @JsonProperty("appointment_letters")
        List<MemberCredentialResponse> appointmentLetters
) {
}