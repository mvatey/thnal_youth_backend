package org.example.tnal_youth_backend.donation.sponsorflow.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.dto.DonationUpdateDTO;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.request.SponsorDonationUpsertRequest;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationPageResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationRowResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorLookupResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.repo.SponsorDonationUiRepo;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SponsorDonationUiService {

    private static final String DONOR_INDIVIDUAL = "INDIVIDUAL";
    private static final String DONOR_INSTITUTION = "INSTITUTION";
    private static final String DONOR_MEMBER = "MEMBER";

    private static final String PAYMENT_MATERIAL = "MATERIAL";

    private final SponsorDonationUiRepo repo;

    private final DonationService donationService;

    private final ExchangeRateService exchangeRateService;

    @Transactional
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
            SponsorDonationUiRepo.SponsorInsert sponsor =
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

        DonationCreateDTO donation =
                new DonationCreateDTO();

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

        DonationCreateResultDTO result =
                donationService.create(donation);

        repo.upsertDetails(
                result.getId(),
                request.getDonorKind(),
                clean(request.getMaterialCategory()),
                normalizedMaterialQuantity(request),
                clean(request.getPurpose())
        );

        return required(result.getId());
    }

    @Transactional
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
            SponsorDonationUiRepo.SponsorInsert sponsor =
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

        DonationUpdateDTO donation =
                new DonationUpdateDTO();

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
                clean(request.getPurpose())
        );

        return required(donationId);
    }

    @Transactional(readOnly = true)
    public SponsorDonationPageResponse list(
            Long branchId,
            String donorKind,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo,
            String search,
            int page,
            int size
    ) {
        validateDateRange(
                paidFrom,
                paidTo
        );

        int safeSize =
                Math.min(
                        Math.max(size, 1),
                        100
                );

        int safePage =
                Math.max(page, 0);

        Long effectiveBranchId =
                effectiveBranchFilter(branchId);

        String normalizedKind =
                normalizeOptionalDonorKind(donorKind);

        String normalizedSearch =
                clean(search);

        List<SponsorDonationRowResponse> items =
                repo.list(
                        effectiveBranchId,
                        normalizedKind,
                        paidFrom,
                        paidTo,
                        normalizedSearch,
                        safeSize,
                        safePage * safeSize
                );

        long total =
                repo.count(
                        effectiveBranchId,
                        normalizedKind,
                        paidFrom,
                        paidTo,
                        normalizedSearch
                );

        return new SponsorDonationPageResponse(
                items,
                total,
                safePage,
                safeSize
        );
    }

    @Transactional(readOnly = true)
    public SponsorDonationRowResponse get(
            Long donationId
    ) {
        return required(donationId);
    }

    @Transactional
    public void delete(
            Long donationId
    ) {
        required(donationId);

        donationService.delete(donationId);
    }

    @Transactional(readOnly = true)
    public SponsorDonationSummaryResponse summary(
            Long branchId,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo
    ) {
        validateDateRange(
                paidFrom,
                paidTo
        );

        Long effectiveBranchId =
                effectiveBranchFilter(branchId);

        SponsorDonationSummaryResponse current =
                repo.summary(
                        effectiveBranchId,
                        paidFrom,
                        paidTo
                );

        normalizeSummary(current);

        if (
                paidFrom == null
                        || paidTo == null
        ) {
            current.setDonationChangePercent(null);
            current.setDonorChangePercent(null);

            return current;
        }

        Duration periodLength =
                Duration.between(
                        paidFrom,
                        paidTo
                );

        OffsetDateTime previousTo =
                paidFrom.minusNanos(1);

        OffsetDateTime previousFrom =
                previousTo.minus(periodLength);

        SponsorDonationSummaryResponse previous =
                repo.summary(
                        effectiveBranchId,
                        previousFrom,
                        previousTo
                );

        normalizeSummary(previous);

        current.setDonationChangePercent(
                percentChange(
                        current.getOverallTotalUsd(),
                        previous.getOverallTotalUsd()
                )
        );

        current.setDonorChangePercent(
                percentChange(
                        BigDecimal.valueOf(
                                current.getDonorCount()
                        ),
                        BigDecimal.valueOf(
                                previous.getDonorCount()
                        )
                )
        );

        return current;
    }

    @Transactional(readOnly = true)
    public List<SponsorLookupResponse> sponsors(
            String search
    ) {
        return repo.sponsors(
                clean(search)
        );
    }

    @Transactional(readOnly = true)
    public List<SponsorLookupResponse> members(
            String search
    ) {
        return repo.members(
                clean(search)
        );
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
                    !repo.activeMemberExists(
                            request.getMemberId()
                    )
            ) {
                throw new BusinessException(
                        "SPONSOR_MEMBER_NOT_FOUND",
                        "The selected active member was not found"
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

        if (
                PAYMENT_MATERIAL.equalsIgnoreCase(
                        paymentMethodCode
                )
        ) {
            if (
                    clean(request.getMaterialCategory()) == null
            ) {
                throw new BusinessException(
                        "MATERIAL_CATEGORY_REQUIRED",
                        "materialCategory is required for MATERIAL payment method"
                );
            }

            if (
                    request.getMaterialQuantity() == null
                            || request.getMaterialQuantity() <= 0
            ) {
                throw new BusinessException(
                        "MATERIAL_QUANTITY_REQUIRED",
                        "materialQuantity must be greater than zero for MATERIAL payment method"
                );
            }
        }

        /*
         * The current donations table requires at least one monetary
         * value to be greater than zero.
         *
         * For MATERIAL donations, send the estimated KHR or USD value.
         */
        if (
                zero(request.getAmountKhr()).signum() <= 0
                        && zero(request.getAmountUsd()).signum() <= 0
        ) {
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

    private SponsorDonationUiRepo.SponsorInsert sponsorRow(
            SponsorDonationUpsertRequest request,
            Long actorId
    ) {
        SponsorDonationUiRepo.SponsorInsert sponsor =
                new SponsorDonationUiRepo.SponsorInsert();

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

    private Integer normalizedMaterialQuantity(
            SponsorDonationUpsertRequest request
    ) {
        String paymentCode =
                paymentCode(
                        request.getPaymentMethodId()
                );

        if (
                PAYMENT_MATERIAL.equalsIgnoreCase(
                        paymentCode
                )
        ) {
            return request.getMaterialQuantity();
        }

        return null;
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

    private void validateDateRange(
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo
    ) {
        if (
                paidFrom != null
                        && paidTo != null
                        && paidFrom.isAfter(paidTo)
        ) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "paidFrom must be before or equal to paidTo"
            );
        }
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