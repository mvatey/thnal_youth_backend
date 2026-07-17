package org.example.tnal_youth_backend.dashboard.dto;

import java.util.List;

public record DashboardBranchesResponse(
        List<DashboardBranchResponse> branches
) {
}