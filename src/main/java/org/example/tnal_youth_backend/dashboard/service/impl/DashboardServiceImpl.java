package org.example.tnal_youth_backend.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;
import org.example.tnal_youth_backend.dashboard.dto.DonationMetricResponse;
import org.example.tnal_youth_backend.dashboard.dto.MetricResponse;
import org.example.tnal_youth_backend.dashboard.repository.DashboardRepository;
import org.example.tnal_youth_backend.dashboard.service.DashboardService;
import org.example.tnal_youth_backend.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final ZoneId CAMBODIA_ZONE =
            ZoneId.of("Asia/Phnom_Penh");

    private final DashboardRepository dashboardRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public DashboardSummaryResponse getSummary(
            YearMonth selectedMonth
    ) {
        YearMonth month = selectedMonth != null
                ? selectedMonth
                : YearMonth.now(CAMBODIA_ZONE);

        User currentUser =
                currentUserProvider.getCurrentUser();

        Long rootBranchId = dashboardRepository
                .findRootBranchId(currentUser.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user is not linked "
                                        + "to a member and branch"
                        )
                );

        List<Long> accessibleBranchIds =
                dashboardRepository.findAccessibleBranchIds(
                        rootBranchId
                );

        if (accessibleBranchIds.isEmpty()) {
            throw new IllegalStateException(
                    "No accessible branches were found"
            );
        }

        LocalDate currentMonthStartDate =
                month.atDay(1);

        LocalDate nextMonthStartDate =
                month.plusMonths(1).atDay(1);

        LocalDate previousMonthStartDate =
                month.minusMonths(1).atDay(1);

        OffsetDateTime currentMonthStart =
                currentMonthStartDate
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime();

        OffsetDateTime nextMonthStart =
                nextMonthStartDate
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime();

        OffsetDateTime previousMonthStart =
                previousMonthStartDate
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime();

        /*
         * Members are cumulative totals.
         *
         * Current: members joined before the next month.
         * Previous: members joined before the current month.
         */
        long currentMembers =
                dashboardRepository.countMembersUntil(
                        accessibleBranchIds,
                        nextMonthStartDate
                );

        long previousMembers =
                dashboardRepository.countMembersUntil(
                        accessibleBranchIds,
                        currentMonthStartDate
                );

        /*
         * Activities are cumulative totals based on starts_at.
         */
        long currentActivities =
                dashboardRepository.countActivitiesUntil(
                        accessibleBranchIds,
                        nextMonthStart
                );

        long previousActivities =
                dashboardRepository.countActivitiesUntil(
                        accessibleBranchIds,
                        currentMonthStart
                );

        /*
         * Donations are monthly totals.
         */
        BigDecimal currentDonationKhr =
                dashboardRepository.sumDonationsKhrBetween(
                        accessibleBranchIds,
                        currentMonthStart,
                        nextMonthStart
                );

        BigDecimal previousDonationKhr =
                dashboardRepository.sumDonationsKhrBetween(
                        accessibleBranchIds,
                        previousMonthStart,
                        currentMonthStart
                );

        BigDecimal currentDonationUsd =
                dashboardRepository.sumDonationsUsdBetween(
                        accessibleBranchIds,
                        currentMonthStart,
                        nextMonthStart
                );

        BigDecimal previousDonationUsd =
                dashboardRepository.sumDonationsUsdBetween(
                        accessibleBranchIds,
                        previousMonthStart,
                        currentMonthStart
                );

        long totalBranches = accessibleBranchIds.size();

        return new DashboardSummaryResponse(
                month.toString(),

                createCountMetric(
                        currentMembers,
                        previousMembers
                ),

                new MetricResponse(
                        totalBranches,
                        null
                ),

                createCountMetric(
                        currentActivities,
                        previousActivities
                ),

                new DonationMetricResponse(
                        currentDonationKhr,
                        currentDonationUsd,
                        calculateChange(
                                currentDonationKhr,
                                previousDonationKhr
                        ),
                        calculateChange(
                                currentDonationUsd,
                                previousDonationUsd
                        )
                )
        );
    }

    private MetricResponse createCountMetric(
            long current,
            long previous
    ) {
        return new MetricResponse(
                current,
                calculateChange(
                        BigDecimal.valueOf(current),
                        BigDecimal.valueOf(previous)
                )
        );
    }

    private BigDecimal calculateChange(
            BigDecimal current,
            BigDecimal previous
    ) {
        BigDecimal safeCurrent =
                current == null ? BigDecimal.ZERO : current;

        BigDecimal safePrevious =
                previous == null ? BigDecimal.ZERO : previous;

        if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
            if (safeCurrent.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO.setScale(2);
            }

            return null;
        }

        return safeCurrent
                .subtract(safePrevious)
                .divide(
                        safePrevious,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}