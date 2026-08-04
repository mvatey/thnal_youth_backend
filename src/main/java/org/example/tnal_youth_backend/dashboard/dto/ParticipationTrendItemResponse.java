package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

@Builder
public record ParticipationTrendItemResponse(
        int month,
        String period,
        long participationCount
) {
}