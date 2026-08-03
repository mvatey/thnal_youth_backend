package org.example.tnal_youth_backend.member.ethnicity.dto.response;

public record EthnicityResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn
) {
}