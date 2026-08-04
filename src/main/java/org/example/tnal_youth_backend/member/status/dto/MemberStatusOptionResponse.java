package org.example.tnal_youth_backend.member.status.dto;

public record MemberStatusOptionResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn
) {
}