package org.example.tnal_youth_backend.activity.income.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeItemRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeSavedItemResponse;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.repo.DonationRepo;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityIncomeService {

    private static final String ACTIVITY_DONATION = "ACTIVITY_DONATION";
    private static final String USD = "USD";
    private static final String KHR = "KHR";

    private final ActivityRepository activityRepository;
    private final DonationRepo donationRepo;
    private final DonationService donationService;
    private final ExchangeRateService exchangeRateService;

    /**
     * Saves all member income rows as ACTIVITY_DONATION records.
     * The whole request is atomic: one invalid item rolls back every insert.
     */
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

        validateUniqueMembers(request.getItems());

        Short donationTypeId = donationRepo.findActiveTypeIdByCode(ACTIVITY_DONATION);
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

        final BigDecimal resolvedRate = exchangeRate;

        List<ActivityIncomeSavedItemResponse> savedItems = request.getItems().stream()
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

    private ActivityIncomeSavedItemResponse saveOne(
            Activity activity,
            Short donationTypeId,
            ActivityIncomeBatchRequest batch,
            ActivityIncomeItemRequest item,
            BigDecimal exchangeRate
    ) {
        DonationCreateDTO donation = new DonationCreateDTO();
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

        DonationCreateResultDTO result = donationService.create(donation);

        return ActivityIncomeSavedItemResponse.builder()
                .donationId(result.getId())
                .donationNo(result.getDonationNo())
                .memberId(item.getMemberId())
                .totalAmountUsd(result.getTotalAmountUsd())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private void validateUniqueMembers(List<ActivityIncomeItemRequest> items) {
        Set<Long> memberIds = new HashSet<>();

        for (ActivityIncomeItemRequest item : items) {
            if (!memberIds.add(item.getMemberId())) {
                throw new BusinessException(
                        "ACTIVITY_INCOME_DUPLICATE_MEMBER",
                        "Member " + item.getMemberId() + " appears more than once in the batch"
                );
            }
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
