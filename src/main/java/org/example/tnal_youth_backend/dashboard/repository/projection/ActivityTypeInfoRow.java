package org.example.tnal_youth_backend.dashboard.repository.projection;

/** One active row of the activity_types lookup, in its own display order. */
public record ActivityTypeInfoRow(
        String code,
        String labelKm,
        String labelEn
) {
}
