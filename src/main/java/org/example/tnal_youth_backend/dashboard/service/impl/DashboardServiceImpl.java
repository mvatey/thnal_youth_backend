package org.example.tnal_youth_backend.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.dto.*;
import org.example.tnal_youth_backend.dashboard.model.DashboardScope;
import org.example.tnal_youth_backend.dashboard.repository.DashboardRepository;
import org.example.tnal_youth_backend.dashboard.repository.projection.ActivityTypeCountRow;
import org.example.tnal_youth_backend.dashboard.repository.projection.DashboardActivityRow;
import org.example.tnal_youth_backend.dashboard.repository.projection.DonationTotals;
import org.example.tnal_youth_backend.dashboard.repository.projection.MonthlyParticipationRow;
import org.example.tnal_youth_backend.dashboard.service.DashboardScopeResolver;
import org.example.tnal_youth_backend.dashboard.service.DashboardService;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;
import org.example.tnal_youth_backend.dashboard.util.DashboardPercentageCalculator;
import org.example.tnal_youth_backend.dashboard.util.DashboardYearResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardScopeResolver dashboardScopeResolver;
    private final DashboardRepository dashboardRepository;
    private final DashboardPercentageCalculator percentageCalculator;
    private final DashboardYearResolver dashboardYearResolver;

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

    @Override
    public DashboardActivitiesResponse getActivities() {

        DashboardScope scope =
                dashboardScopeResolver.resolve(null);

        boolean organizationWide =
                scope.branchId() == null;

        OffsetDateTime now =
                OffsetDateTime.now(
                        ZoneId.of(
                                "Asia/Phnom_Penh"
                        )
                );

        List<DashboardActivityRow>
                recentCompletedRows;

        List<DashboardActivityRow>
                upcomingRows;

        if (organizationWide) {

            recentCompletedRows =
                    dashboardRepository
                            .findRecentCompletedActivities();

            upcomingRows =
                    dashboardRepository
                            .findUpcomingActivities(now);

        } else {

            recentCompletedRows =
                    dashboardRepository
                            .findRecentCompletedActivitiesByBranch(
                                    scope.branchId()
                            );

            upcomingRows =
                    dashboardRepository
                            .findUpcomingActivitiesByBranch(
                                    scope.branchId(),
                                    now
                            );
        }

        return DashboardActivitiesResponse.builder()
                .recentCompleted(
                        recentCompletedRows.stream()
                                .map(this::toActivityResponse)
                                .toList()
                )
                .upcoming(
                        upcomingRows.stream()
                                .map(this::toActivityResponse)
                                .toList()
                )
                .build();
    }

    private DashboardActivityItemResponse
    toActivityResponse(
            DashboardActivityRow row
    ) {
        return DashboardActivityItemResponse.builder()
                .id(row.id())
                .titleKm(row.titleKm())
                .titleEn(row.titleEn())
                .coverImage(row.coverImage())
                .startsAt(row.startsAt())
                .endsAt(row.endsAt())
                .locationName(row.locationName())
                .type(row.type())
                .participantCount(
                        row.participantCount()
                )
                .build();
    }

    @Override
    public ActivityTypeBreakdownResponse
    getActivityTypeBreakdown(String month) {

        DashboardScope scope =
                dashboardScopeResolver.resolve(month);

        DashboardMonthRange range =
                scope.monthRange();

        List<ActivityTypeCountRow> rows;

        if (scope.branchId() == null) {
            rows =
                    dashboardRepository
                            .findActivityTypeBreakdown(
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );
        } else {
            rows =
                    dashboardRepository
                            .findActivityTypeBreakdownByBranch(
                                    scope.branchId(),
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );
        }

        Map<String, Long> counts =
                rows.stream()
                        .collect(
                                Collectors.toMap(
                                        row -> row.type()
                                                .toUpperCase(),
                                        ActivityTypeCountRow::count
                                )
                        );

        long internal =
                counts.getOrDefault(
                        "INTERNAL",
                        0L
                );

        long external =
                counts.getOrDefault(
                        "EXTERNAL",
                        0L
                );

        return ActivityTypeBreakdownResponse.builder()
                .period(range.period())
                .internal(internal)
                .external(external)
                .total(internal + external)
                .build();
    }

    @Override
    public ParticipationTrendResponse
    getParticipationTrend(Integer year) {

        int selectedYear =
                dashboardYearResolver.resolve(year);

        ZoneId cambodiaZone =
                ZoneId.of("Asia/Phnom_Penh");

        OffsetDateTime start =
                LocalDate.of(
                                selectedYear,
                                1,
                                1
                        )
                        .atStartOfDay(cambodiaZone)
                        .toOffsetDateTime();

        OffsetDateTime end =
                LocalDate.of(
                                selectedYear + 1,
                                1,
                                1
                        )
                        .atStartOfDay(cambodiaZone)
                        .toOffsetDateTime();

        DashboardScope scope =
                dashboardScopeResolver.resolve(null);

        List<MonthlyParticipationRow> rows;

        if (scope.branchId() == null) {
            rows =
                    dashboardRepository
                            .findParticipationTrend(
                                    start,
                                    end
                            );
        } else {
            rows =
                    dashboardRepository
                            .findParticipationTrendByBranch(
                                    scope.branchId(),
                                    start,
                                    end
                            );
        }

        Map<Integer, Long> countsByMonth =
                rows.stream()
                        .collect(
                                Collectors.toMap(
                                        MonthlyParticipationRow::month,
                                        MonthlyParticipationRow
                                                ::participationCount
                                )
                        );

        List<ParticipationTrendItemResponse> months =
                IntStream.rangeClosed(1, 12)
                        .mapToObj(monthNumber ->
                                ParticipationTrendItemResponse
                                        .builder()
                                        .month(monthNumber)
                                        .period(
                                                String.format(
                                                        "%d-%02d",
                                                        selectedYear,
                                                        monthNumber
                                                )
                                        )
                                        .participationCount(
                                                countsByMonth
                                                        .getOrDefault(
                                                                monthNumber,
                                                                0L
                                                        )
                                        )
                                        .build()
                        )
                        .toList();

        return ParticipationTrendResponse.builder()
                .year(selectedYear)
                .months(months)
                .build();
    }

}