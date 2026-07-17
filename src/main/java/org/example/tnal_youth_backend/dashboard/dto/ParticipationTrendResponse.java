package org.example.tnal_youth_backend.dashboard.dto;

import java.util.List;

public record ParticipationTrendResponse(
        Integer year,
        String countingMethod,
        List<MonthlyParticipationItem> data
) {

    public record MonthlyParticipationItem(
            Integer month,
            String labelKm,
            String labelEn,
            Long participantCount
    ) {
    }
}