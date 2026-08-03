package org.example.tnal_youth_backend.donation.monthly.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationBatchRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationItemRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationBatchResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationMemberPageResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationSavedItemResponse;
import org.example.tnal_youth_backend.donation.monthly.repo.MonthlyDonationRepo;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MonthlyDonationService {

    private static final String USD = "USD";
    private static final String KHR = "KHR";

    private final MonthlyDonationRepo monthlyDonationRepo;
    private final DonationService donationService;
    private final ExchangeRateService exchangeRateService;

    public MonthlyDonationMemberPageResponse listMembers(
            Long branchId,
            LocalDate donationPeriod,
            String search,
            int page,
            int size
    ) {
        validatePage(page, size);
        validatePeriod(donationPeriod);

        String normalizedSearch = normalizeToNull(search);
        int offset = page * size;

        return MonthlyDonationMemberPageResponse.builder()
                .branchId(branchId)
                .donationPeriod(donationPeriod)
                .items(monthlyDonationRepo.listMembers(
                        branchId,
                        donationPeriod,
                        normalizedSearch,
                        size,
                        offset
                ))
                .total(monthlyDonationRepo.countMembers(branchId, normalizedSearch))
                .page(page)
                .size(size)
                .build();
    }

    @Transactional
    public MonthlyDonationBatchResponse createBatch(MonthlyDonationBatchRequest request) {
        validatePeriod(request.getDonationPeriod());
        validateUniqueMembers(request.getItems());

        Short donationTypeId = monthlyDonationRepo.findMonthlyDonationTypeId();
        if (donationTypeId == null) {
            throw new BusinessException(
                    "MONTHLY_DONATION_TYPE_NOT_FOUND",
                    "Active donation type MONTHLY_DONATION was not found"
            );
        }

        for (MonthlyDonationItemRequest item : request.getItems()) {
            if (monthlyDonationRepo.countExistingMonthlyDonation(
                    item.getMemberId(),
                    request.getBranchId(),
                    request.getDonationPeriod()
            ) > 0) {
                throw new BusinessException(
                        "MONTHLY_DONATION_ALREADY_EXISTS",
                        "Member " + item.getMemberId()
                                + " already has a monthly donation for "
                                + request.getDonationPeriod()
                );
            }
        }

        boolean containsKhr = request.getItems().stream()
                .map(MonthlyDonationItemRequest::getAmountKhr)
                .filter(value -> value != null)
                .anyMatch(value -> value.signum() > 0);

        BigDecimal exchangeRate = null;
        if (containsKhr) {
            ExchangeRateResponse rate = exchangeRateService.getRateForDate(
                    USD,
                    KHR,
                    request.getPaidAt().toLocalDate()
            );
            exchangeRate = rate.getRate();
        }

        BigDecimal resolvedRate = exchangeRate;

        List<MonthlyDonationSavedItemResponse> savedItems = request.getItems().stream()
                .map(item -> saveOne(request, item, donationTypeId, resolvedRate))
                .toList();

        BigDecimal totalKhr = request.getItems().stream()
                .map(item -> zeroIfNull(item.getAmountKhr()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsd = request.getItems().stream()
                .map(item -> zeroIfNull(item.getAmountUsd()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallTotalUsd = savedItems.stream()
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

    private MonthlyDonationSavedItemResponse saveOne(
            MonthlyDonationBatchRequest batch,
            MonthlyDonationItemRequest item,
            Short donationTypeId,
            BigDecimal exchangeRate
    ) {
        DonationCreateDTO donation = new DonationCreateDTO();
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

        DonationCreateResultDTO result = donationService.create(donation);

        return MonthlyDonationSavedItemResponse.builder()
                .donationId(result.getId())
                .donationNo(result.getDonationNo())
                .memberId(item.getMemberId())
                .totalAmountUsd(result.getTotalAmountUsd())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private void validateUniqueMembers(List<MonthlyDonationItemRequest> items) {
        Set<Long> ids = new HashSet<>();
        for (MonthlyDonationItemRequest item : items) {
            if (!ids.add(item.getMemberId())) {
                throw new BusinessException(
                        "MONTHLY_DONATION_DUPLICATE_MEMBER",
                        "Member " + item.getMemberId() + " appears more than once"
                );
            }
        }
    }

    private void validatePeriod(LocalDate period) {
        if (period == null || period.getDayOfMonth() != 1) {
            throw new BusinessException(
                    "MONTHLY_DONATION_INVALID_PERIOD",
                    "donationPeriod must be the first day of the selected month"
            );
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("INVALID_PAGE", "page must be zero or positive");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException("INVALID_PAGE_SIZE", "size must be between 1 and 100");
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
