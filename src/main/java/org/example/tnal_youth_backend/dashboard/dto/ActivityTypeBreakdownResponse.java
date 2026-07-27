package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

@Builder
public record ActivityTypeBreakdownResponse(
        String period,
        long internal,
        long external,
        long total
) {
}