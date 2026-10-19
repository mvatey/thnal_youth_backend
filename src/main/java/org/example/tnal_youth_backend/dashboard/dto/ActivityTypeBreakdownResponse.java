package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ActivityTypeBreakdownResponse(
        String period,
        long internal,
        long external,
        long total,

        /*
         * Every ACTIVE activity type (zero counts included) in the lookup's
         * own order, so a type added later on the variable page shows up
         * here without a code change. internal/external above are kept for
         * older clients; total now covers all types.
         */
        List<TypeCount> types
) {

    public record TypeCount(
            String code,
            String labelKm,
            String labelEn,
            long count
    ) {
    }
}