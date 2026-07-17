package org.example.tnal_youth_backend.dashboard.dto;

public record DashboardBranchResponse(
        Long id,
        String nameKm,
        String nameEn,
        Long parentBranchId
) {
}