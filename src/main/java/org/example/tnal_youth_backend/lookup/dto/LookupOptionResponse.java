package org.example.tnal_youth_backend.lookup.dto;

public record LookupOptionResponse<T>(
        T value,
        String code,
        String labelKm,
        String labelEn
) {
}