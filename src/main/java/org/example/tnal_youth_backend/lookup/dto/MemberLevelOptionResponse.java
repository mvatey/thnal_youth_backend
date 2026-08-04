package org.example.tnal_youth_backend.lookup.dto;

public record MemberLevelOptionResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn
) {
}