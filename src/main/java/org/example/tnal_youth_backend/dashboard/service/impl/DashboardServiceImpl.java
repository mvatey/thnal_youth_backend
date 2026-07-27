package org.example.tnal_youth_backend.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryData;
import org.example.tnal_youth_backend.dashboard.dto.DashboardSummaryResponse;
import org.example.tnal_youth_backend.dashboard.dto.DonationMetricResponse;
import org.example.tnal_youth_backend.dashboard.dto.MetricResponse;
import org.example.tnal_youth_backend.dashboard.model.DashboardScope;
import org.example.tnal_youth_backend.dashboard.repository.DashboardRepository;
import org.example.tnal_youth_backend.dashboard.repository.projection.DonationTotals;
import org.example.tnal_youth_backend.dashboard.service.DashboardScopeResolver;
import org.example.tnal_youth_backend.dashboard.service.DashboardService;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;
import org.example.tnal_youth_backend.dashboard.util.DashboardPercentageCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardScopeResolver dashboardScopeResolver;
    private final DashboardRepository dashboardRepository;
    private final DashboardPercentageCalculator percentageCalculator;

    @Override
    public DashboardSummaryResponse getSummary(String month) {

        DashboardScope scope =
                dashboardScopeResolver.resolve(month);

        DashboardMonthRange range =
                scope.monthRange();

        boolean organizationWide =
                scope.organizationWide();

        Long branchId =
                scope.branchId();

        long currentMembers;
        long previousMembers;
        long activeBranches;
        long currentActivities;
        long previousActivities;

        DonationTotals currentDonations;
        DonationTotals previousDonations;

        if (organizationWide) {

            currentMembers =
                    dashboardRepository
                            .countAllActiveMembersBefore(
                                    range.nextMonthStartDate()
                            );

            previousMembers =
                    dashboardRepository
                            .countAllActiveMembersBefore(
                                    range.selectedMonthStartDate()
                            );

            activeBranches =
                    dashboardRepository
                            .countAllActiveBranches();

            currentActivities =
                    dashboardRepository
                            .countAllActivitiesBefore(
                                    range.nextMonthStart()
                            );

            previousActivities =
                    dashboardRepository
                            .countAllActivitiesBefore(
                                    range.selectedMonthStart()
                            );

            currentDonations =
                    dashboardRepository
                            .sumAllDonationsBetween(
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

            previousDonations =
                    dashboardRepository
                            .sumAllDonationsBetween(
                                    range.previousMonthStart(),
                                    range.selectedMonthStart()
                            );

        } else {

            currentMembers =
                    dashboardRepository
                            .countActiveMembersByBranchBefore(
                                    branchId,
                                    range.nextMonthStartDate()
                            );

            previousMembers =
                    dashboardRepository
                            .countActiveMembersByBranchBefore(
                                    branchId,
                                    range.selectedMonthStartDate()
                            );

            activeBranches =
                    dashboardRepository
                            .countActiveBranchById(branchId);

            currentActivities =
                    dashboardRepository
                            .countActivitiesByBranchBefore(
                                    branchId,
                                    range.nextMonthStart()
                            );

            previousActivities =
                    dashboardRepository
                            .countActivitiesByBranchBefore(
                                    branchId,
                                    range.selectedMonthStart()
                            );

            currentDonations =
                    dashboardRepository
                            .sumDonationsByBranchBetween(
                                    branchId,
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

            previousDonations =
                    dashboardRepository
                            .sumDonationsByBranchBetween(
                                    branchId,
                                    range.previousMonthStart(),
                                    range.selectedMonthStart()
                            );
        }

        DashboardSummaryData summary =
                DashboardSummaryData.builder()
                        .members(
                                MetricResponse.builder()
                                        .value(currentMembers)
                                        .changePercent(
                                                percentageCalculator.calculate(
                                                        currentMembers,
                                                        previousMembers
                                                )
                                        )
                                        .build()
                        )
                        .branches(
                                MetricResponse.builder()
                                        .value(activeBranches)
                                        .changePercent(null)
                                        .build()
                        )
                        .activities(
                                MetricResponse.builder()
                                        .value(currentActivities)
                                        .changePercent(
                                                percentageCalculator.calculate(
                                                        currentActivities,
                                                        previousActivities
                                                )
                                        )
                                        .build()
                        )
                        .donations(
                                DonationMetricResponse.builder()
                                        .amountKhr(
                                                currentDonations.amountKhr()
                                        )
                                        .amountUsd(
                                                currentDonations.amountUsd()
                                        )
                                        .changePercentKhr(
                                                percentageCalculator.calculate(
                                                        currentDonations.amountKhr(),
                                                        previousDonations.amountKhr()
                                                )
                                        )
                                        .changePercentUsd(
                                                percentageCalculator.calculate(
                                                        currentDonations.amountUsd(),
                                                        previousDonations.amountUsd()
                                                )
                                        )
                                        .build()
                        )
                        .build();

        return DashboardSummaryResponse.builder()
                .period(range.period())
                .summary(summary)
                .build();
    }
}