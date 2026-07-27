package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ParticipationTrendResponse(
        int year,
        List<ParticipationTrendItemResponse> months
) {
}