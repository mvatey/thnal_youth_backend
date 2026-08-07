package org.example.tnal_youth_backend.donation.monthly.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.request.DonationCreateRequest;
import org.example.tnal_youth_backend.donation.dto.response.DonationCreateResultResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationBatchRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationItemRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationBatchResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationBranchResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationDetailResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationMemberPageResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationPageResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationRowResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationSavedItemResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.monthly.repository.MonthlyDonationRepository;
import org.example.tnal_youth_backend.donation.monthly.service.MonthlyDonationService;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MonthlyDonationServiceImpl implements MonthlyDonationService {

    private static final String USD = "USD";
    private static final String KHR = "KHR";
    private static final String ROLE_BRANCH_LEADER = "BRANCH_LEADER";

    private final MonthlyDonationRepository monthlyDonationRepository;
    private final DonationService donationService;
    private final ExchangeRateService exchangeRateService;

    @Override
    @Transactional(readOnly = true)
    public MonthlyDonationMemberPageResponse listMembers(
            Long branchId,
            Integer month,
            Integer year,
            String search,
            int page,
            int size
    ) {
        validatePage(page, size);
        validateRequiredMonthYear(month, year);
        enforceBranchAccess(branchId);

        LocalDate donationPeriod =
                LocalDate.of(year, month, 1);

        String normalizedSearch =
                normalizeToNull(search);

        int offset = page * size;

        return MonthlyDonationMemberPageResponse.builder()
                .branchId(branchId)
                .donationPeriod(donationPeriod)
                .items(
                        monthlyDonationRepository.listMembers(
                                branchId,
                                donationPeriod,
                                normalizedSearch,
                                size,
                                offset
                        )
                )
                .total(
                        monthlyDonationRepository.countMembers(
                                branchId,
                                normalizedSearch
                        )
                )
                .page(page)
                .size(size)
                .build();
    }

    @Override
    @Transactional
    public MonthlyDonationBatchResponse createBatch(
            MonthlyDonationBatchRequest request
    ) {
        validatePeriod(request.getDonationPeriod());
        enforceBranchAccess(request.getBranchId());
        validateUniqueMembers(request.getItems());

        Short donationTypeId =
                monthlyDonationRepository.findMonthlyDonationTypeId();

        if (donationTypeId == null) {
            throw new BusinessException(
                    "MONTHLY_DONATION_TYPE_NOT_FOUND",
                    "Active donation type MONTHLY_DONATION was not found"
            );
        }

        for (MonthlyDonationItemRequest item : request.getItems()) {
            int existingCount =
                    monthlyDonationRepository.countExistingMonthlyDonation(
                            item.getMemberId(),
                            request.getBranchId(),
                            request.getDonationPeriod()
                    );

            if (existingCount > 0) {
                throw new BusinessException(
                        "MONTHLY_DONATION_ALREADY_EXISTS",
                        "Member "
                                + item.getMemberId()
                                + " already has a monthly donation for "
                                + request.getDonationPeriod()
                );
            }
        }

        boolean containsKhr = request.getItems()
                .stream()
                .map(MonthlyDonationItemRequest::getAmountKhr)
                .filter(value -> value != null)
                .anyMatch(value -> value.signum() > 0);

        BigDecimal exchangeRate = null;

        if (containsKhr) {
            ExchangeRateResponse rate =
                    exchangeRateService.getRateForDate(
                            USD,
                            KHR,
                            request.getPaidAt().toLocalDate()
                    );

            exchangeRate = rate.getRate();
        }

        BigDecimal resolvedRate = exchangeRate;

        List<MonthlyDonationSavedItemResponse> savedItems =
                request.getItems()
                        .stream()
                        .map(item -> saveOne(
                                request,
                                item,
                                donationTypeId,
                                resolvedRate
                        ))
                        .toList();

        BigDecimal totalKhr = request.getItems()
                .stream()
                .map(item -> zeroIfNull(item.getAmountKhr()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsd = request.getItems()
                .stream()
                .map(item -> zeroIfNull(item.getAmountUsd()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallTotalUsd = savedItems
                .stream()
                .map(MonthlyDonationSavedItemResponse::getTotalAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MonthlyDonationBatchResponse.builder()
                .branchId(request.getBranchId())
                .donationPeriod(request.getDonationPeriod())
                .paidAt(request.getPaidAt())
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
    public MonthlyDonationPageResponse listMonthlyDonations(
            Long branchId,
            Integer month,
            Integer year,
            String search,
            int page,
            int size
    ) {
        validatePage(page, size);
        validateMonthYear(month, year);

        Long effectiveBranchId =
                effectiveBranchFilter(branchId);

        String normalizedSearch =
                normalizeToNull(search);

        int offset = page * size;

        return new MonthlyDonationPageResponse(
                monthlyDonationRepository.listMonthlyDonationGroups(
                        effectiveBranchId,
                        month,
                        year,
                        normalizedSearch,
                        size,
                        offset
                ),
                monthlyDonationRepository.countMonthlyDonationGroups(
                        effectiveBranchId,
                        month,
                        year,
                        normalizedSearch
                ),
                page,
                size
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyDonationDetailResponse getMonthlyDonationDetail(
            Long branchId,
            LocalDate donationPeriod
    ) {
        validatePeriod(donationPeriod);
        enforceBranchAccess(branchId);

        MonthlyDonationBranchResponse branch =
                monthlyDonationRepository.findBranch(branchId);

        if (branch == null) {
            throw new BusinessException(
                    "BRANCH_NOT_FOUND",
                    "Branch " + branchId + " does not exist"
            );
        }

        MonthlyDonationSummaryResponse summary =
                monthlyDonationRepository.summarizeMonthlyDonations(
                        branchId,
                        donationPeriod
                );

        if (summary == null) {
            summary = new MonthlyDonationSummaryResponse(
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        List<MonthlyDonationRowResponse> items =
                monthlyDonationRepository.findMonthlyDonationRows(
                        branchId,
                        donationPeriod
                );

        return new MonthlyDonationDetailResponse(
                branch,
                donationPeriod,
                summary,
                items
        );
    }

    @Override
    @Transactional
    public void deleteMonthlyDonation(Long donationId) {
        if (donationId == null || donationId < 1) {
            throw new BusinessException(
                    "MONTHLY_DONATION_INVALID_ID",
                    "donationId must be a positive number"
            );
        }

        Long branchId =
                monthlyDonationRepository.findMonthlyDonationBranchId(
                        donationId
                );

        if (branchId == null) {
            throw new BusinessException(
                    "MONTHLY_DONATION_NOT_FOUND",
                    "Monthly donation "
                            + donationId
                            + " was not found"
            );
        }

        enforceBranchAccess(branchId);

        int deletedRows =
                monthlyDonationRepository.deleteMonthlyDonation(
                        donationId
                );

        if (deletedRows == 0) {
            throw new BusinessException(
                    "MONTHLY_DONATION_DELETE_FAILED",
                    "Monthly donation "
                            + donationId
                            + " could not be deleted"
            );
        }
    }

    private void validateRequiredMonthYear(
            Integer month,
            Integer year
    ) {
        if (month == null) {
            throw new BusinessException(
                    "MONTH_REQUIRED",
                    "month is required"
            );
        }

        if (year == null) {
            throw new BusinessException(
                    "YEAR_REQUIRED",
                    "year is required"
            );
        }

        if (month < 1 || month > 12) {
            throw new BusinessException(
                    "INVALID_MONTH",
                    "month must be between 1 and 12"
            );
        }

        if (year < 2000 || year > 2100) {
            throw new BusinessException(
                    "INVALID_YEAR",
                    "year must be between 2000 and 2100"
            );
        }
    }

    private MonthlyDonationSavedItemResponse saveOne(
            MonthlyDonationBatchRequest batch,
            MonthlyDonationItemRequest item,
            Short donationTypeId,
            BigDecimal exchangeRate
    ) {
        DonationCreateRequest donation =
                new DonationCreateRequest();

        donation.setDonationTypeId(donationTypeId);
        donation.setMemberId(item.getMemberId());
        donation.setSponsorId(null);
        donation.setDonorName(null);
        donation.setActivityId(null);
        donation.setBranchId(batch.getBranchId());
        donation.setDonationPeriod(batch.getDonationPeriod());
        donation.setAmountKhr(item.getAmountKhr());
        donation.setAmountUsd(item.getAmountUsd());
        donation.setExchangeRateKhrPerUsd(exchangeRate);
        donation.setPaymentMethodId(item.getPaymentMethodId());
        donation.setPaidAt(batch.getPaidAt());
        donation.setPaymentReference(item.getPaymentReference());
        donation.setReceiptFileId(item.getReceiptFileId());
        donation.setNote(item.getDescription());
        donation.setClientRequestId(item.getClientRequestId());

        DonationCreateResultResponse result =
                donationService.create(donation);

        return MonthlyDonationSavedItemResponse.builder()
                .donationId(result.getId())
                .donationNo(result.getDonationNo())
                .memberId(item.getMemberId())
                .totalAmountUsd(result.getTotalAmountUsd())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private Long effectiveBranchFilter(
            Long requestedBranchId
    ) {
        Long scopedBranchId =
                scopedBranchIdOrNull();

        return scopedBranchId != null
                ? scopedBranchId
                : requestedBranchId;
    }

    private void enforceBranchAccess(
            Long requestedBranchId
    ) {
        Long scopedBranchId =
                scopedBranchIdOrNull();

        if (scopedBranchId != null
                && !scopedBranchId.equals(requestedBranchId)) {
            throw new AccessDeniedException(
                    "This branch is outside your permitted scope"
            );
        }
    }

    private Long scopedBranchIdOrNull() {
        String currentRole =
                SecurityUtils.getCurrentUserRole();

        if (!ROLE_BRANCH_LEADER.equals(currentRole)) {
            return null;
        }

        Long branchId =
                monthlyDonationRepository.findBranchIdByUserId(
                        SecurityUtils.getCurrentUserId()
                );

        if (branchId == null) {
            throw new AccessDeniedException(
                    "Your account is a branch leader but is not assigned to a branch"
            );
        }

        return branchId;
    }

    private void validateUniqueMembers(
            List<MonthlyDonationItemRequest> items
    ) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(
                    "MONTHLY_DONATION_ITEMS_REQUIRED",
                    "At least one monthly donation item is required"
            );
        }

        Set<Long> ids = new HashSet<>();

        for (MonthlyDonationItemRequest item : items) {
            if (item.getMemberId() == null) {
                throw new BusinessException(
                        "MONTHLY_DONATION_MEMBER_REQUIRED",
                        "memberId is required"
                );
            }

            if (!ids.add(item.getMemberId())) {
                throw new BusinessException(
                        "MONTHLY_DONATION_DUPLICATE_MEMBER",
                        "Member "
                                + item.getMemberId()
                                + " appears more than once"
                );
            }
        }
    }

    private void validatePeriod(
            LocalDate period
    ) {
        if (period == null) {
            throw new BusinessException(
                    "MONTHLY_DONATION_PERIOD_REQUIRED",
                    "donationPeriod is required"
            );
        }

        if (period.getDayOfMonth() != 1) {
            throw new BusinessException(
                    "MONTHLY_DONATION_INVALID_PERIOD",
                    "donationPeriod must be the first day of the selected month"
            );
        }
    }

    private void validateMonthYear(
            Integer month,
            Integer year
    ) {
        if (month != null
                && (month < 1 || month > 12)) {
            throw new BusinessException(
                    "INVALID_MONTH",
                    "month must be between 1 and 12"
            );
        }

        if (year != null
                && (year < 2000 || year > 2100)) {
            throw new BusinessException(
                    "INVALID_YEAR",
                    "year must be between 2000 and 2100"
            );
        }

        if (month != null && year == null) {
            throw new BusinessException(
                    "YEAR_REQUIRED",
                    "year is required when month is selected"
            );
        }
    }

    private void validatePage(
            int page,
            int size
    ) {
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

    private BigDecimal zeroIfNull(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String normalizeToNull(
            String value
    ) {
        return value == null || value.isBlank()
                ? null
                : value.strip();
    }
}