package org.example.tnal_youth_backend.activity.income.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeActivityResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeMemberRowResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeSummaryResponse;
import org.example.tnal_youth_backend.activity.income.repo.ActivityIncomeReadRepo;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityIncomeReadService {

    private static final String ROLE_BRANCH_LEADER = "BRANCH_LEADER";

    private final ActivityIncomeReadRepo repo;

    @Transactional(readOnly = true)
    public ActivityIncomePageResponse list(
            Long branchId,
            String search,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo,
            int page,
            int size
    ) {
        validateDateRange(paidFrom, paidTo);

        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, 100);
        String safeSearch = normalizeSearch(search);
        Long effectiveBranchId = effectiveBranchFilter(branchId);

        var items = repo.listGrouped(
                effectiveBranchId,
                safeSearch,
                paidFrom,
                paidTo,
                safeSize,
                safePage * safeSize
        );

        long total = repo.countGrouped(
                effectiveBranchId,
                safeSearch,
                paidFrom,
                paidTo
        );

        return new ActivityIncomePageResponse(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public ActivityIncomeDetailResponse getDetail(Long activityId) {
        ActivityIncomeActivityResponse activity = repo.findActivity(activityId);
        if (activity == null) {
            throw new BusinessException(
                    "ACTIVITY_NOT_FOUND",
                    "Activity " + activityId + " does not exist"
            );
        }

        Long scopedBranchId = scopedBranchIdOrNull();
        if (scopedBranchId != null && !scopedBranchId.equals(activity.getBranchId())) {
            throw new AccessDeniedException("This activity belongs to another branch");
        }

        ActivityIncomeSummaryResponse summary = repo.summarize(activityId);
        if (summary == null) {
            summary = new ActivityIncomeSummaryResponse(
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        List<ActivityIncomeMemberRowResponse> items = repo.findRows(activityId);
        return new ActivityIncomeDetailResponse(activity, summary, items);
    }

    private Long effectiveBranchFilter(Long requestedBranchId) {
        Long scope = scopedBranchIdOrNull();
        return scope != null ? scope : requestedBranchId;
    }

    private Long scopedBranchIdOrNull() {
        if (!ROLE_BRANCH_LEADER.equals(SecurityUtils.getCurrentUserRole())) {
            return null;
        }

        Long actorId = SecurityUtils.getCurrentUserId();
        Long branchId = repo.findBranchIdByUserId(actorId);
        if (branchId == null) {
            throw new AccessDeniedException(
                    "Your account is a branch leader but is not assigned to a branch"
            );
        }
        return branchId;
    }

    private void validateDateRange(OffsetDateTime paidFrom, OffsetDateTime paidTo) {
        if (paidFrom != null && paidTo != null && paidFrom.isAfter(paidTo)) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "paidFrom must be before or equal to paidTo"
            );
        }
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.strip()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
