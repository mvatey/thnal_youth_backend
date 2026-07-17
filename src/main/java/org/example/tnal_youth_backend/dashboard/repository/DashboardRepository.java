package org.example.tnal_youth_backend.dashboard.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository {

    Optional<Long> findRootBranchId(Long userId);

    List<Long> findAccessibleBranchIds(Long rootBranchId);

    long countMembersUntil(
            List<Long> branchIds,
            LocalDate endExclusive
    );

    long countActivitiesUntil(
            List<Long> branchIds,
            OffsetDateTime endExclusive
    );

    BigDecimal sumDonationsKhrBetween(
            List<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    );

    BigDecimal sumDonationsUsdBetween(
            List<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    );
}