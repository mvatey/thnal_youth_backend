package org.example.tnal_youth_backend.lookup.dto;

public record NationalityOptionResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn
) {
}