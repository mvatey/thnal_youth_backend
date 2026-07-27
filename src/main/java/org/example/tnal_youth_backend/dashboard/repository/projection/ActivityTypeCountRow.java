package org.example.tnal_youth_backend.dashboard.repository.projection;

public record ActivityTypeCountRow(
        String type,
        long count
) {
}