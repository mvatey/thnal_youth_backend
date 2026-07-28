package org.example.tnal_youth_backend.dashboard.repository.projection;

public record MonthlyParticipationRow(
        int month,
        long participationCount
) {
}