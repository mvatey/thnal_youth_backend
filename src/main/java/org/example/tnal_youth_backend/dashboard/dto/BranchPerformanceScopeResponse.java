package org.example.tnal_youth_backend.dashboard.dto;

import lombok.Builder;

@Builder
public record BranchPerformanceScopeResponse(
        Long branchId,
        String branchNameKm,
        String branchNameEn,
        boolean combined
) {
}

