package org.example.tnal_youth_backend.donation.sponsorflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.request.DonationCreateRequest;
import org.example.tnal_youth_backend.donation.dto.response.DonationCreateResultResponse;
import org.example.tnal_youth_backend.donation.dto.request.DonationUpdateRequest;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.request.SponsorDonationUpsertRequest;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationPageResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationRowResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorLookupResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.repository.SponsorDonationRepository;
import org.example.tnal_youth_backend.donation.sponsorflow.service.SponsorDonationService;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SponsorDonationServiceImpl implements SponsorDonationService {

    private static final String DONOR_INDIVIDUAL = "INDIVIDUAL";
    private static final String DONOR_INSTITUTION = "INSTITUTION";
    private static final String DONOR_MEMBER = "MEMBER";

    private final SponsorDonationRepository repo;

    private final DonationService donationService;

    private final ExchangeRateService exchangeRateService;

    @Transactional
    @Override
    public SponsorDonationRowResponse create(
            SponsorDonationUpsertRequest request
    ) {
        normalizeDonorKind(request);
        validate(request);

        Long actorId = SecurityUtils.getCurrentUserId();

        Long effectiveBranchId =
                resolveWritableBranch(request.getBranchId());

        Long sponsorId = null;
        Long memberId = null;

        if (DONOR_MEMBER.equals(request.getDonorKind())) {
            memberId = request.getMemberId();
        } else if (request.getSponsorId() != null) {
            /*
             * Existing sponsor selected from the UI.
             * Do not update its shared profile during donation creation.
             */
            sponsorId = request.getSponsorId();
        } else {
            SponsorDonationRepository.SponsorInsert sponsor =
                    sponsorRow(
                            request,
                            actorId
                    );

            repo.insertSponsor(sponsor);

            sponsorId = sponsor.id;

            if (sponsorId == null) {
                throw new BusinessException(
                        "SPONSOR_INSERT_FAILED",
                        "Failed to create sponsor"
                );
            }
        }

        DonationCreateRequest donation =
                new DonationCreateRequest();

        donation.setDonationTypeId(
                requiredTypeId()
        );

        donation.setMemberId(memberId);

        donation.setSponsorId(sponsorId);

        donation.setDonorName(null);

        donation.setActivityId(
                request.getActivityId()
        );

        donation.setBranchId(
                effectiveBranchId
        );

        donation.setAmountKhr(
                zero(request.getAmountKhr())
        );

        donation.setAmountUsd(
                zero(request.getAmountUsd())
        );

        donation.setExchangeRateKhrPerUsd(
                exchangeRate(request)
        );

        donation.setPaymentMethodId(
                request.getPaymentMethodId()
        );

        donation.setPaidAt(
                request.getPaidAt()
        );

        donation.setPaymentReference(
                clean(request.getPaymentReference())
        );

        donation.setReceiptFileId(
                request.getReceiptFileId()
        );

        donation.setNote(
                clean(request.getNote())
        );

        donation.setClientRequestId(
                clean(request.getClientRequestId())
        );

        DonationCreateResultResponse result =
                donationService.create(donation);

        repo.upsertDetails(
                result.getId(),
                request.getDonorKind(),
                clean(request.getMaterialCategory()),
                normalizedMaterialQuantity(request),
                clean(request.getMaterialQuantityType()),
                clean(request.getPurpose())
        );

        return required(result.getId());
    }

    @Transactional
    @Override
    public SponsorDonationRowResponse update(
            Long donationId,
            SponsorDonationUpsertRequest request
    ) {
        normalizeDonorKind(request);
        validate(request);

        SponsorDonationRowResponse current =
                required(donationId);

        Long effectiveBranchId =
                resolveWritableBranch(request.getBranchId());

        Long sponsorId = null;
        Long memberId = null;

        if (DONOR_MEMBER.equals(request.getDonorKind())) {
            memberId = request.getMemberId();
        } else if (request.getSponsorId() != null) {
            /*
             * Existing sponsor selection.
             * Do not silently change the sponsor profile shared by
             * previous donation records.
             */
            sponsorId = request.getSponsorId();
        } else if (
                current.getSponsorId() != null
                        && !DONOR_MEMBER.equals(current.getDonorKind())
        ) {
            /*
             * The current donation already owns a created sponsor.
             * Keep the same sponsor reference.
             *
             * Profile editing can be handled later by a dedicated
             * sponsor profile endpoint.
             */
            sponsorId = current.getSponsorId();
        } else {
            SponsorDonationRepository.SponsorInsert sponsor =
                    sponsorRow(
                            request,
                            SecurityUtils.getCurrentUserId()
                    );

            repo.insertSponsor(sponsor);

            sponsorId = sponsor.id;

            if (sponsorId == null) {
                throw new BusinessException(
                        "SPONSOR_INSERT_FAILED",
                        "Failed to create sponsor"
                );
            }
        }

        DonationUpdateRequest donation =
                new DonationUpdateRequest();

        donation.setDonationTypeId(
                requiredTypeId()
        );

        donation.setMemberId(memberId);

        donation.setSponsorId(sponsorId);

        donation.setDonorName(null);

        donation.setActivityId(
                request.getActivityId()
        );

        donation.setBranchId(
                effectiveBranchId
        );

        donation.setAmountKhr(
                zero(request.getAmountKhr())
        );

        donation.setAmountUsd(
                zero(request.getAmountUsd())
        );

        donation.setExchangeRateKhrPerUsd(
                exchangeRate(request)
        );

        donation.setPaymentMethodId(
                request.getPaymentMethodId()
        );

        donation.setPaidAt(
                request.getPaidAt()
        );

        donation.setPaymentReference(
                clean(request.getPaymentReference())
        );

        donation.setReceiptFileId(
                request.getReceiptFileId()
        );

        donation.setNote(
                clean(request.getNote())
        );

        donation.setExpectedUpdatedAt(
                request.getExpectedUpdatedAt()
        );

        donationService.update(
                donationId,
                donation
        );

        repo.upsertDetails(
                donationId,
                request.getDonorKind(),
                clean(request.getMaterialCategory()),
                normalizedMaterialQuantity(request),
                clean(request.getMaterialQuantityType()),
                clean(request.getPurpose())
        );

        return required(donationId);
    }

    @Transactional(readOnly = true)
    @Override
    public SponsorDonationPageResponse list(
            Long branchId,
            String donorKind,
            LocalDate paidDate,
            String search,
            int page,
            int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Long effectiveBranchId = effectiveBranchFilter(branchId);
        String normalizedKind = normalizeOptionalDonorKind(donorKind);
        String normalizedSearch = clean(search);

        OffsetDateTime paidFrom = startOfDay(paidDate);
        OffsetDateTime paidTo = endOfDay(paidDate);

        List<SponsorDonationRowResponse> items = repo.list(
                effectiveBranchId,
                normalizedKind,
                paidFrom,
                paidTo,
                normalizedSearch,
                safeSize,
                safePage * safeSize
        );

        long total = repo.count(
                effectiveBranchId,
                normalizedKind,
                paidFrom,
                paidTo,
                normalizedSearch
        );

        return new SponsorDonationPageResponse(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    @Override
    public SponsorDonationRowResponse get(
            Long donationId
    ) {
        return required(donationId);
    }

    @Transactional
    @Override
    public void delete(
            Long donationId
    ) {
        required(donationId);

        donationService.delete(donationId);
    }

    @Transactional(readOnly = true)
    @Override
    public SponsorDonationSummaryResponse summary(
            Long branchId,
            LocalDate paidDate
    ) {
        Long effectiveBranchId = effectiveBranchFilter(branchId);
        OffsetDateTime paidFrom = startOfDay(paidDate);
        OffsetDateTime paidTo = endOfDay(paidDate);

        SponsorDonationSummaryResponse current = repo.summary(
                effectiveBranchId,
                paidFrom,
                paidTo
        );
        normalizeSummary(current);

        if (paidDate == null) {
            current.setDonationChangePercent(null);
            current.setDonorChangePercent(null);
            return current;
        }

        LocalDate previousDate = paidDate.minusDays(1);
        SponsorDonationSummaryResponse previous = repo.summary(
                effectiveBranchId,
                startOfDay(previousDate),
                endOfDay(previousDate)
        );
        normalizeSummary(previous);

        current.setDonationChangePercent(percentChange(
                current.getOverallTotalUsd(),
                previous.getOverallTotalUsd()
        ));
        current.setDonorChangePercent(percentChange(
                BigDecimal.valueOf(current.getDonorCount()),
                BigDecimal.valueOf(previous.getDonorCount())
        ));
        return current;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SponsorLookupResponse> sponsors(
            String search
    ) {
        return repo.sponsors(
                clean(search)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<SponsorLookupResponse> members(
            Long branchId,
            String search
    ) {
        if (branchId == null || !repo.branchExists(branchId)) {
            throw new BusinessException(
                    "BRANCH_NOT_FOUND",
                    "The selected branch does not exist"
            );
        }
        Long effectiveBranchId = resolveWritableBranch(branchId);
        return repo.members(effectiveBranchId, clean(search));
    }

    private SponsorDonationRowResponse required(
            Long donationId
    ) {
        SponsorDonationRowResponse row =
                repo.findOne(donationId);

        if (row == null) {
            throw new BusinessException(
                    "SPONSOR_DONATION_NOT_FOUND",
                    "Sponsor donation "
                            + donationId
                            + " was not found"
            );
        }

        Long scopeBranchId =
                scopedBranchIdOrNull();

        if (
                scopeBranchId != null
                        && !scopeBranchId.equals(row.getBranchId())
        ) {
            throw new AccessDeniedException(
                    "Donation is outside your branch"
            );
        }

        return row;
    }

    private void validate(
            SponsorDonationUpsertRequest request
    ) {
        if (
                request.getBranchId() == null
                        || !repo.branchExists(request.getBranchId())
        ) {
            throw new BusinessException(
                    "BRANCH_NOT_FOUND",
                    "The selected branch does not exist"
            );
        }

        if (
                request.getActivityId() != null
                        && !repo.activityExists(request.getActivityId())
        ) {
            throw new BusinessException(
                    "ACTIVITY_NOT_FOUND",
                    "The selected activity does not exist"
            );
        }

        if (
                request.getPaidAt() == null
        ) {
            throw new BusinessException(
                    "SPONSOR_PAID_AT_REQUIRED",
                    "paidAt is required"
            );
        }

        String paymentMethodCode =
                paymentCode(request.getPaymentMethodId());

        if (paymentMethodCode == null) {
            throw new BusinessException(
                    "PAYMENT_METHOD_NOT_FOUND",
                    "The selected payment method does not exist"
            );
        }

        if (
                DONOR_MEMBER.equals(request.getDonorKind())
        ) {
            if (request.getMemberId() == null) {
                throw new BusinessException(
                        "SPONSOR_MEMBER_REQUIRED",
                        "memberId is required for MEMBER donorKind"
                );
            }

            if (
                    !repo.activeMemberExistsInBranch(
                            request.getMemberId(),
                            request.getBranchId()
                    )
            ) {
                throw new BusinessException(
                        "SPONSOR_MEMBER_NOT_FOUND",
                        "The selected active member was not found in the selected branch"
                );
            }

            if (request.getSponsorId() != null) {
                throw new BusinessException(
                        "SPONSOR_SOURCE_INVALID",
                        "sponsorId must be empty when donorKind is MEMBER"
                );
            }
        } else {
            if (request.getMemberId() != null) {
                throw new BusinessException(
                        "SPONSOR_SOURCE_INVALID",
                        "memberId is only allowed when donorKind is MEMBER"
                );
            }

            if (
                    request.getSponsorId() == null
                            && clean(request.getName()) == null
            ) {
                throw new BusinessException(
                        "SPONSOR_NAME_REQUIRED",
                        "name is required for a new external sponsor"
                );
            }

            if (
                    request.getSponsorId() != null
                            && !repo.sponsorExists(
                            request.getSponsorId()
                    )
            ) {
                throw new BusinessException(
                        "SPONSOR_NOT_FOUND",
                        "The selected sponsor was not found"
                );
            }
        }

        validateMaterialFields(request);

        /* Money remains required as before. Material is additional information. */
        if (zero(request.getAmountKhr()).signum() <= 0
                && zero(request.getAmountUsd()).signum() <= 0) {
            throw new BusinessException(
                    "DONATION_AMOUNT_REQUIRED",
                    "At least one of amountKhr or amountUsd must be greater than zero"
            );
        }
    }


    private String paymentCode(
            Short paymentMethodId
    ) {
        if (paymentMethodId == null) {
            return null;
        }

        return repo.paymentMethodCode(
                paymentMethodId
        );
    }

    private BigDecimal exchangeRate(
            SponsorDonationUpsertRequest request
    ) {
        if (
                zero(request.getAmountKhr()).signum() <= 0
        ) {
            return null;
        }

        return exchangeRateService
                .getRateForDate(
                        "USD",
                        "KHR",
                        request.getPaidAt().toLocalDate()
                )
                .getRate();
    }

    private Short requiredTypeId() {
        Short typeId =
                repo.typeId();

        if (typeId == null) {
            throw new BusinessException(
                    "SPONSOR_DONATION_TYPE_NOT_FOUND",
                    "SPONSOR_DONATION donation type is missing"
            );
        }

        return typeId;
    }

    private Long resolveWritableBranch(
            Long requestedBranchId
    ) {
        Long scopeBranchId =
                scopedBranchIdOrNull();

        if (
                scopeBranchId != null
                        && !scopeBranchId.equals(requestedBranchId)
        ) {
            throw new AccessDeniedException(
                    "You may only record donations for your own branch"
            );
        }

        return requestedBranchId;
    }

    private Long effectiveBranchFilter(
            Long requestedBranchId
    ) {
        Long scopeBranchId =
                scopedBranchIdOrNull();

        if (scopeBranchId != null) {
            return scopeBranchId;
        }

        return requestedBranchId;
    }

    private Long scopedBranchIdOrNull() {
        Long actorId =
                SecurityUtils.getCurrentUserId();

        String role =
                repo.userRole(actorId);

        if (
                !"BRANCH_LEADER".equals(role)
        ) {
            return null;
        }

        Long branchId =
                repo.userBranch(actorId);

        if (branchId == null) {
            throw new AccessDeniedException(
                    "Branch leader account is not linked to a branch"
            );
        }

        return branchId;
    }

    private SponsorDonationRepository.SponsorInsert sponsorRow(
            SponsorDonationUpsertRequest request,
            Long actorId
    ) {
        SponsorDonationRepository.SponsorInsert sponsor =
                new SponsorDonationRepository.SponsorInsert();

        String sponsorTypeCode =
                DONOR_INSTITUTION.equals(
                        request.getDonorKind()
                )
                        ? "ORGANIZATION"
                        : "INDIVIDUAL";

        sponsor.typeId =
                repo.sponsorTypeId(
                        sponsorTypeCode
                );

        if (sponsor.typeId == null) {
            throw new BusinessException(
                    "SPONSOR_TYPE_NOT_FOUND",
                    "Sponsor type "
                            + sponsorTypeCode
                            + " is missing"
            );
        }

        sponsor.name =
                clean(request.getName());

        sponsor.phone =
                clean(request.getPhone());

        sponsor.email =
                clean(request.getEmail());

        sponsor.address =
                clean(request.getAddress());

        sponsor.note =
                clean(request.getNote());

        sponsor.actorId =
                actorId;

        return sponsor;
    }

    private BigDecimal normalizedMaterialQuantity(
            SponsorDonationUpsertRequest request
    ) {
        return hasAnyMaterialField(request)
                ? request.getMaterialQuantity()
                : null;
    }

    private void validateMaterialFields(SponsorDonationUpsertRequest request) {
        if (!hasAnyMaterialField(request)) {
            return;
        }

        if (clean(request.getMaterialCategory()) == null) {
            throw new BusinessException(
                    "MATERIAL_CATEGORY_REQUIRED",
                    "materialCategory is required when material is provided"
            );
        }

        if (request.getMaterialQuantity() == null
                || request.getMaterialQuantity().signum() <= 0) {
            throw new BusinessException(
                    "MATERIAL_QUANTITY_REQUIRED",
                    "materialQuantity must be greater than zero when material is provided"
            );
        }

        if (clean(request.getMaterialQuantityType()) == null) {
            throw new BusinessException(
                    "MATERIAL_QUANTITY_TYPE_REQUIRED",
                    "materialQuantityType is required when material is provided"
            );
        }
    }

    private boolean hasAnyMaterialField(SponsorDonationUpsertRequest request) {
        return clean(request.getMaterialCategory()) != null
                || request.getMaterialQuantity() != null
                || clean(request.getMaterialQuantityType()) != null;
    }

    private OffsetDateTime startOfDay(LocalDate date) {
        return date == null
                ? null
                : date.atStartOfDay().atOffset(ZoneOffset.ofHours(7));
    }

    private OffsetDateTime endOfDay(LocalDate date) {
        return date == null
                ? null
                : date.plusDays(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.ofHours(7))
                .minusNanos(1);
    }

    private void normalizeDonorKind(
            SponsorDonationUpsertRequest request
    ) {
        if (
                request.getDonorKind() != null
        ) {
            request.setDonorKind(
                    request.getDonorKind()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }
    }

    private String normalizeOptionalDonorKind(
            String donorKind
    ) {
        String normalized =
                clean(donorKind);

        if (normalized == null) {
            return null;
        }

        normalized =
                normalized.toUpperCase(Locale.ROOT);

        if (
                !DONOR_INDIVIDUAL.equals(normalized)
                        && !DONOR_INSTITUTION.equals(normalized)
                        && !DONOR_MEMBER.equals(normalized)
        ) {
            throw new BusinessException(
                    "SPONSOR_DONOR_KIND_INVALID",
                    "donorKind must be INDIVIDUAL, INSTITUTION, or MEMBER"
            );
        }

        return normalized;
    }

    private void normalizeSummary(
            SponsorDonationSummaryResponse summary
    ) {
        if (summary.getTotalKhr() == null) {
            summary.setTotalKhr(
                    BigDecimal.ZERO
            );
        }

        if (summary.getTotalUsd() == null) {
            summary.setTotalUsd(
                    BigDecimal.ZERO
            );
        }

        if (summary.getOverallTotalUsd() == null) {
            summary.setOverallTotalUsd(
                    BigDecimal.ZERO
            );
        }
    }

    private BigDecimal percentChange(
            BigDecimal current,
            BigDecimal previous
    ) {
        BigDecimal safeCurrent =
                current == null
                        ? BigDecimal.ZERO
                        : current;

        BigDecimal safePrevious =
                previous == null
                        ? BigDecimal.ZERO
                        : previous;

        if (
                safePrevious.signum() == 0
        ) {
            if (
                    safeCurrent.signum() == 0
            ) {
                return BigDecimal.ZERO;
            }

            /*
             * No previous baseline exists. Returning null is more
             * truthful than displaying a misleading percentage.
             */
            return null;
        }

        return safeCurrent
                .subtract(safePrevious)
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        safePrevious,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private static BigDecimal zero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private static String clean(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}