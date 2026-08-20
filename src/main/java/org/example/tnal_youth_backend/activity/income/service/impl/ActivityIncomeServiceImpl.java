package org.example.tnal_youth_backend.activity.income.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeItemRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeActivityResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeMemberRowResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeSavedItemResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeSummaryResponse;
import org.example.tnal_youth_backend.activity.income.repository.ActivityIncomeRepository;
import org.example.tnal_youth_backend.activity.income.service.ActivityIncomeService;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.request.DonationCreateRequest;
import org.example.tnal_youth_backend.donation.dto.response.DonationCreateResultResponse;
import org.example.tnal_youth_backend.donation.repository.DonationRepository;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityIncomeServiceImpl implements ActivityIncomeService {

    private static final String ACTIVITY_DONATION = "ACTIVITY_DONATION";
    private static final String USD = "USD";
    private static final String KHR = "KHR";
    private static final String ROLE_BRANCH_LEADER = "BRANCH_LEADER";
    private static final String ROLE_SECRETARY = "SECRETARY";

    private final ActivityRepository activityRepository;
    private final ActivityIncomeRepository activityIncomeRepository;
    private final StaffBranchScopeService staffBranchScopeService;
    private final DonationRepository donationRepository;
    private final DonationService donationService;
    private final ExchangeRateService exchangeRateService;

    @Override
    @Transactional
    public ActivityIncomeBatchResponse createBatch(
            Long activityId,
            ActivityIncomeBatchRequest request
    ) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(
                        "ACTIVITY_NOT_FOUND",
                        "Activity " + activityId + " does not exist"
                ));

        enforceBranchAccess(activity.getBranchId());
        validateUniqueMembers(request.getItems());

        Short donationTypeId =
                donationRepository.findActiveTypeIdByCode(ACTIVITY_DONATION);

        if (donationTypeId == null) {
            throw new BusinessException(
                    "ACTIVITY_DONATION_TYPE_NOT_FOUND",
                    "Active donation type ACTIVITY_DONATION was not found"
            );
        }

        boolean containsKhr = request.getItems().stream()
                .map(ActivityIncomeItemRequest::getAmountKhr)
                .filter(value -> value != null)
                .anyMatch(value -> value.signum() > 0);

        BigDecimal exchangeRate = null;
        if (containsKhr) {
            ExchangeRateResponse rate = exchangeRateService.getRateForDate(
                    USD,
                    KHR,
                    request.getReceivedAt().toLocalDate()
            );
            exchangeRate = rate.getRate();
        }

        BigDecimal resolvedRate = exchangeRate;

        List<ActivityIncomeSavedItemResponse> savedItems =
                request.getItems().stream()
                        .map(item -> saveOne(
                                activity,
                                donationTypeId,
                                request,
                                item,
                                resolvedRate
                        ))
                        .toList();

        BigDecimal totalKhr = request.getItems().stream()
                .map(item -> zeroIfNull(item.getAmountKhr()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsd = request.getItems().stream()
                .map(item -> zeroIfNull(item.getAmountUsd()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallTotalUsd = savedItems.stream()
                .map(ActivityIncomeSavedItemResponse::getTotalAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ActivityIncomeBatchResponse.builder()
                .activityId(activity.getId())
                .activityName(activity.getTitleKm())
                .branchId(activity.getBranchId())
                .receivedAt(request.getReceivedAt())
                .exchangeRateKhrPerUsd(exchangeRate)
                .savedCount(savedItems.size())
                .totalKhr(totalKhr)
                .totalUsd(totalUsd)
                .overallTotalUsd(overallTotalUsd)
                .items(savedItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityIncomePageResponse list(
            Long branchId,
            String search,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo,
            int page,
            int size
    ) {
        validatePage(page, size);
        validateDateRange(paidFrom, paidTo);

        String normalizedSearch = normalizeSearch(search);
        Long effectiveBranchId = effectiveBranchFilter(branchId);
        int offset = page * size;

        return new ActivityIncomePageResponse(
                activityIncomeRepository.listGrouped(
                        effectiveBranchId,
                        normalizedSearch,
                        paidFrom,
                        paidTo,
                        size,
                        offset
                ),
                activityIncomeRepository.countGrouped(
                        effectiveBranchId,
                        normalizedSearch,
                        paidFrom,
                        paidTo
                ),
                page,
                size
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityIncomeDetailResponse getDetail(Long activityId) {
        ActivityIncomeActivityResponse activity =
                activityIncomeRepository.findActivity(activityId);

        if (activity == null) {
            throw new BusinessException(
                    "ACTIVITY_NOT_FOUND",
                    "Activity " + activityId + " does not exist"
            );
        }

        enforceBranchAccess(activity.getBranchId());

        ActivityIncomeSummaryResponse summary =
                activityIncomeRepository.summarize(activityId);

        if (summary == null) {
            summary = new ActivityIncomeSummaryResponse(
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        List<ActivityIncomeMemberRowResponse> items =
                activityIncomeRepository.findRows(activityId);

        return new ActivityIncomeDetailResponse(
                activity,
                summary,
                items
        );
    }

    @Override
    @Transactional
    public void deleteIncome(Long activityId, Long donationId) {
        Long branchId = activityIncomeRepository.findIncomeBranchId(
                activityId,
                donationId
        );

        if (branchId == null) {
            throw new BusinessException(
                    "ACTIVITY_INCOME_NOT_FOUND",
                    "Income donation " + donationId
                            + " was not found for activity " + activityId
            );
        }

        enforceBranchAccess(branchId);

        int deletedRows = activityIncomeRepository.deleteIncome(
                activityId,
                donationId
        );

        if (deletedRows == 0) {
            throw new BusinessException(
                    "ACTIVITY_INCOME_DELETE_FAILED",
                    "Activity income could not be deleted"
            );
        }
    }

    private ActivityIncomeSavedItemResponse saveOne(
            Activity activity,
            Short donationTypeId,
            ActivityIncomeBatchRequest batch,
            ActivityIncomeItemRequest item,
            BigDecimal exchangeRate
    ) {
        DonationCreateRequest donation = new DonationCreateRequest();
        donation.setDonationTypeId(donationTypeId);
        donation.setMemberId(item.getMemberId());
        donation.setSponsorId(null);
        donation.setDonorName(null);
        donation.setActivityId(activity.getId());
        donation.setBranchId(activity.getBranchId());
        donation.setDonationPeriod(null);
        donation.setAmountKhr(item.getAmountKhr());
        donation.setAmountUsd(item.getAmountUsd());
        donation.setExchangeRateKhrPerUsd(exchangeRate);
        donation.setPaymentMethodId(item.getPaymentMethodId());
        donation.setPaidAt(batch.getReceivedAt());
        donation.setPaymentReference(item.getPaymentReference());
        donation.setReceiptFileId(item.getReceiptFileId());
        donation.setNote(item.getDescription());
        donation.setClientRequestId(item.getClientRequestId());

        DonationCreateResultResponse result = donationService.create(donation);

        return ActivityIncomeSavedItemResponse.builder()
                .donationId(result.getId())
                .donationNo(result.getDonationNo())
                .memberId(item.getMemberId())
                .totalAmountUsd(result.getTotalAmountUsd())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private Long effectiveBranchFilter(Long requestedBranchId) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!ROLE_BRANCH_LEADER.equals(role) && !ROLE_SECRETARY.equals(role)) {
            return requestedBranchId;
        }

        Set<Long> allowed = staffBranchScopeService.currentStaffBranchIds();
        if (requestedBranchId == null) {
            if (allowed.size() == 1) {
                return allowed.iterator().next();
            }
            throw new AccessDeniedException("Select one of your assigned branches");
        }
        if (!allowed.contains(requestedBranchId)) {
            throw new AccessDeniedException("This branch is outside your permitted scope");
        }
        return requestedBranchId;
    }

    private void enforceBranchAccess(Long requestedBranchId) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!ROLE_BRANCH_LEADER.equals(role) && !ROLE_SECRETARY.equals(role)) {
            return;
        }
        if (!staffBranchScopeService.currentStaffBranchIds().contains(requestedBranchId)) {
            throw new AccessDeniedException("This branch is outside your permitted scope");
        }
    }

    private void validateUniqueMembers(
            List<ActivityIncomeItemRequest> items
    ) {
        Set<Long> memberIds = new HashSet<>();

        for (ActivityIncomeItemRequest item : items) {
            if (!memberIds.add(item.getMemberId())) {
                throw new BusinessException(
                        "ACTIVITY_INCOME_DUPLICATE_MEMBER",
                        "Member " + item.getMemberId()
                                + " appears more than once in the batch"
                );
            }
        }
    }

    private void validateDateRange(
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo
    ) {
        if (paidFrom != null
                && paidTo != null
                && paidFrom.isAfter(paidTo)) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "paidFrom must be before or equal to paidTo"
            );
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException(
                    "INVALID_PAGE",
                    "page must be zero or positive"
            );
        }

        if (size < 1 || size > 100) {
            throw new BusinessException(
                    "INVALID_PAGE_SIZE",
                    "size must be between 1 and 100"
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

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
