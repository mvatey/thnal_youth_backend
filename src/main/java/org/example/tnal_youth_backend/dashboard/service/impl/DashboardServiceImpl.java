package org.example.tnal_youth_backend.dashboard.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.dto.*;
import org.example.tnal_youth_backend.dashboard.exception.DashboardAccessException;
import org.example.tnal_youth_backend.dashboard.model.DashboardScope;
import org.example.tnal_youth_backend.dashboard.repository.DashboardRepository;
import org.example.tnal_youth_backend.dashboard.repository.projection.*;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final ZoneId CAMBODIA_ZONE =
            ZoneId.of("Asia/Phnom_Penh");

    private final DashboardScopeResolver dashboardScopeResolver;
    private final DashboardRepository dashboardRepository;
    private final DashboardPercentageCalculator percentageCalculator;
    private final DashboardYearResolver dashboardYearResolver;

    // =========================================================
    // SUMMARY
    // =========================================================

    @Override
    public DashboardSummaryResponse getSummary(
            String month
    ) {
        DashboardScope scope =
                dashboardScopeResolver.resolve(month);

        DashboardMonthRange range =
                scope.monthRange();

        Collection<Long> branchIds =
                scope.accessibleBranchIds();

        long currentMembers;
        long previousMembers;
        long activeBranches;
        long currentActivities;
        long previousActivities;

        DonationTotals currentDonations;
        DonationTotals previousDonations;

        if (scope.organizationWide()) {

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
                            .countActiveMembersByBranchesBefore(
                                    branchIds,
                                    range.nextMonthStartDate()
                            );

            previousMembers =
                    dashboardRepository
                            .countActiveMembersByBranchesBefore(
                                    branchIds,
                                    range.selectedMonthStartDate()
                            );

            activeBranches =
                    dashboardRepository
                            .countActiveBranchesByIds(
                                    branchIds
                            );

            currentActivities =
                    dashboardRepository
                            .countActivitiesByBranchesBefore(
                                    branchIds,
                                    range.nextMonthStart()
                            );

            previousActivities =
                    dashboardRepository
                            .countActivitiesByBranchesBefore(
                                    branchIds,
                                    range.selectedMonthStart()
                            );

            currentDonations =
                    dashboardRepository
                            .sumDonationsByBranchesBetween(
                                    branchIds,
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

            previousDonations =
                    dashboardRepository
                            .sumDonationsByBranchesBetween(
                                    branchIds,
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

    // =========================================================
    // RECENT AND UPCOMING ACTIVITIES
    // =========================================================

    @Override
    public DashboardActivitiesResponse getActivities() {

        DashboardScope scope =
                dashboardScopeResolver.resolve(null);

        OffsetDateTime now =
                OffsetDateTime.now(CAMBODIA_ZONE);

        List<DashboardActivityRow> recentCompletedRows;
        List<DashboardActivityRow> upcomingRows;

        if (scope.organizationWide()) {

            recentCompletedRows =
                    dashboardRepository
                            .findRecentCompletedActivities();

            upcomingRows =
                    dashboardRepository
                            .findUpcomingActivities(now);

        } else {

            recentCompletedRows =
                    dashboardRepository
                            .findRecentCompletedActivitiesByBranches(
                                    scope.accessibleBranchIds()
                            );

            upcomingRows =
                    dashboardRepository
                            .findUpcomingActivitiesByBranches(
                                    scope.accessibleBranchIds(),
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

    private DashboardActivityItemResponse toActivityResponse(
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

    // =========================================================
    // ACTIVITY TYPE BREAKDOWN
    // =========================================================

    @Override
    public ActivityTypeBreakdownResponse
    getActivityTypeBreakdown(
            String month
    ) {
        DashboardScope scope =
                dashboardScopeResolver.resolve(month);

        DashboardMonthRange range =
                scope.monthRange();

        List<ActivityTypeCountRow> rows;

        if (scope.organizationWide()) {

            rows =
                    dashboardRepository
                            .findActivityTypeBreakdown(
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

        } else {

            rows =
                    dashboardRepository
                            .findActivityTypeBreakdownByBranches(
                                    scope.accessibleBranchIds(),
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );
        }

        Map<String, Long> counts =
                rows.stream()
                        .collect(
                                Collectors.toMap(
                                        row ->
                                                row.type()
                                                        .trim()
                                                        .toUpperCase(),
                                        ActivityTypeCountRow::count,
                                        Long::sum
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

    // =========================================================
    // PARTICIPATION TREND
    // =========================================================

    @Override
    public ParticipationTrendResponse
    getParticipationTrend(
            Integer year
    ) {
        int selectedYear =
                dashboardYearResolver.resolve(year);

        OffsetDateTime start =
                LocalDate.of(
                                selectedYear,
                                1,
                                1
                        )
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime();

        OffsetDateTime end =
                LocalDate.of(
                                selectedYear + 1,
                                1,
                                1
                        )
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime();

        DashboardScope scope =
                dashboardScopeResolver.resolve(null);

        List<MonthlyParticipationRow> rows;

        if (scope.organizationWide()) {

            rows =
                    dashboardRepository
                            .findParticipationTrend(
                                    start,
                                    end
                            );

        } else {

            rows =
                    dashboardRepository
                            .findParticipationTrendByBranches(
                                    scope.accessibleBranchIds(),
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
                                                ::participationCount,
                                        Long::sum
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

    private Collection<Long> resolvePerformanceBranchIds(
            DashboardScope scope,
            Long requestedBranchId
    ) {
        /*
         * Admin without a specific branch uses
         * organization-wide repository queries.
         */
        if (scope.organizationWide()
                && requestedBranchId == null) {

            return List.of();
        }

        /*
         * Admin may select any existing branch.
         */
        if (scope.organizationWide()) {

            dashboardRepository
                    .findBranchById(requestedBranchId)
                    .orElseThrow(() ->
                            new DashboardAccessException(
                                    "The selected branch could not be found."
                            )
                    );

            return List.of(requestedBranchId);
        }

        /*
         * Secretary or branch leader omitted branchId:
         * combine all accessible assigned branches.
         */
        if (requestedBranchId == null) {
            return scope.accessibleBranchIds();
        }

        /*
         * Secretary or branch leader selected a branch:
         * verify it belongs to their accessible scope.
         */
        if (!scope.canAccessBranch(
                requestedBranchId
        )) {
            throw new DashboardAccessException(
                    "You do not have access to the selected branch."
            );
        }

        return List.of(requestedBranchId);
    }

    private BranchPerformanceScopeResponse
    buildBranchPerformanceScope(
            DashboardScope scope,
            Long requestedBranchId
    ) {
        /*
         * Combined organization-wide result.
         */
        if (scope.organizationWide()
                && requestedBranchId == null) {

            return BranchPerformanceScopeResponse.builder()
                    .branchId(null)
                    .branchNameKm("សាខាទាំងអស់")
                    .branchNameEn("All Branches")
                    .combined(true)
                    .build();
        }

        /*
         * Combined assigned-branch result.
         */
        if (!scope.organizationWide()
                && requestedBranchId == null) {

            return BranchPerformanceScopeResponse.builder()
                    .branchId(null)
                    .branchNameKm(
                            "សាខាដែលទទួលខុសត្រូវ"
                    )
                    .branchNameEn(
                            "Assigned Branches"
                    )
                    .combined(
                            scope.accessibleBranchIds()
                                    .size() > 1
                    )
                    .build();
        }

        DashboardBranchRow branch =
                dashboardRepository
                        .findBranchById(
                                requestedBranchId
                        )
                        .orElseThrow(() ->
                                new DashboardAccessException(
                                        "The selected branch could not be found."
                                )
                        );

        return BranchPerformanceScopeResponse.builder()
                .branchId(branch.id())
                .branchNameKm(branch.nameKm())
                .branchNameEn(branch.nameEn())
                .combined(false)
                .build();
    }

    @Override
    public BranchPerformanceResponse getBranchPerformance(
            Long branchId,
            String month
    ) {
        DashboardScope scope =
                dashboardScopeResolver.resolve(month);

        DashboardMonthRange range =
                scope.monthRange();

        Collection<Long> selectedBranchIds =
                resolvePerformanceBranchIds(
                        scope,
                        branchId
                );

        boolean organizationWide =
                scope.organizationWide()
                        && branchId == null;

        long currentActivities;
        long previousActivities;

        long currentMembers;
        long previousMembers;

        DonationTotals currentDonations;
        DonationTotals previousDonations;

        if (organizationWide) {

            currentActivities =
                    dashboardRepository
                            .countAllActivitiesBetween(
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

            previousActivities =
                    dashboardRepository
                            .countAllActivitiesBetween(
                                    range.previousMonthStart(),
                                    range.selectedMonthStart()
                            );

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

            currentActivities =
                    dashboardRepository
                            .countActivitiesByBranchesBetween(
                                    selectedBranchIds,
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

            previousActivities =
                    dashboardRepository
                            .countActivitiesByBranchesBetween(
                                    selectedBranchIds,
                                    range.previousMonthStart(),
                                    range.selectedMonthStart()
                            );

            currentMembers =
                    dashboardRepository
                            .countActiveMembersByBranchesBefore(
                                    selectedBranchIds,
                                    range.nextMonthStartDate()
                            );

            previousMembers =
                    dashboardRepository
                            .countActiveMembersByBranchesBefore(
                                    selectedBranchIds,
                                    range.selectedMonthStartDate()
                            );

            currentDonations =
                    dashboardRepository
                            .sumDonationsByBranchesBetween(
                                    selectedBranchIds,
                                    range.selectedMonthStart(),
                                    range.nextMonthStart()
                            );

            previousDonations =
                    dashboardRepository
                            .sumDonationsByBranchesBetween(
                                    selectedBranchIds,
                                    range.previousMonthStart(),
                                    range.selectedMonthStart()
                            );
        }

        return BranchPerformanceResponse.builder()
                .period(range.period())
                .scope(
                        buildBranchPerformanceScope(
                                scope,
                                branchId
                        )
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
                .build();
    }

}