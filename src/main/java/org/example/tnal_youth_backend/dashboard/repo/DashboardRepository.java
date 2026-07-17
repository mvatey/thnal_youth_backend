package org.example.tnal_youth_backend.dashboard.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository {

    long countMembers(
            List<Long> branchIds,
            LocalDateTime start,
            LocalDateTime end
    );

    long countBranchesCreated(
            List<Long> branchIds,
            LocalDateTime start,
            LocalDateTime end
    );

    long countActivities(
            List<Long> branchIds,
            LocalDateTime start,
            LocalDateTime end
    );

    BigDecimal sumDonationsUsd(
            List<Long> branchIds,
            LocalDateTime start,
            LocalDateTime end
    );
}