package org.example.tnal_youth_backend.donation.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.dto.DonationDTO;
import org.example.tnal_youth_backend.donation.dto.DonationPageDTO;
import org.example.tnal_youth_backend.donation.dto.DonationSummaryDTO;
import org.example.tnal_youth_backend.donation.dto.DonationUpdateDTO;
import org.example.tnal_youth_backend.donation.model.DonationModel;
import org.example.tnal_youth_backend.donation.repo.DonationRepo;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Donation recording + reporting service.
 *
 * <p>Written to the SAME conventions as {@code NotificationService}: constructor
 * injection, {@link BusinessException} with a stable machine code for every
 * business rule (so {@code GlobalExceptionHandler} maps it to a 400 with a
 * traceable {@code errorCode}), and {@link SecurityUtils#getCurrentUserId()} for
 * sender/recorder attribution.
 *
 * <p><b>Trust boundary.</b> The DTO bean-validation only covers shape/range.
 * The cross-field invariants the schema models as CHECK constraints
 * ({@code chk_donation_source}, {@code chk_donation_amounts},
 * {@code chk_donation_exchange_rate}) are re-validated here BEFORE the insert so
 * the caller gets a specific code instead of a generic constraint violation. The
 * DB constraints remain the ultimate authority — this is defence in depth, not a
 * replacement.
 *
 * <p><b>Money.</b> {@code totalAmountUsd} is ALWAYS computed server-side and never
 * read from the client:
 * {@code totalUsd = amountUsd + (amountKhr / exchangeRateKhrPerUsd)}, rounded to
 * 2 dp (HALF_UP) to fit NUMERIC(14,2). When there is no KHR component the rate is
 * not required and the total is just the USD amount.
 *
 * <p><b>Idempotency (V23).</b> If the caller supplies {@code clientRequestId}, a
 * sequential replay from the same recorder returns the original donation instead
 * of inserting again (pre-check below). Truly concurrent duplicates are stopped
 * by the partial unique index {@code uq_donations_recorder_client_request}: the
 * losing insert raises a {@code DataIntegrityViolationException} which the global
 * handler renders as 400 {@code DATA_INTEGRITY_VIOLATION}. Omitting the key keeps
 * the plain non-idempotent behaviour.
 */
@Service
@RequiredArgsConstructor
public class DonationService {

    private static final DateTimeFormatter DONATION_NO_DATE =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    /** Donation type codes that carry an extra required field (see DTO javadoc). */
    private static final String TYPE_ACTIVITY_DONATION = "ACTIVITY_DONATION";
    private static final String TYPE_MONTHLY_DONATION = "MONTHLY_DONATION";

    private final DonationRepo repo;
    private final Clock clock; // utcClock bean from TimeConfig

    // ===================================================================
    // create
    // ===================================================================

    @Transactional
    public DonationCreateResultDTO create(DonationCreateDTO req) {
        Long actorId = SecurityUtils.getCurrentUserId();
        String clientRequestId = normalizeToNull(req.getClientRequestId());

        // ---- idempotency short-circuit (already-committed replay) ----
        if (clientRequestId != null) {
            Long existing = repo.findIdByRecorderAndClientRequestId(actorId, clientRequestId);
            if (existing != null) {
                return buildResult(existing);
            }
        }

        Prepared p = validateAndPrepare(
                req.getDonationTypeId(), req.getMemberId(), req.getSponsorId(), req.getDonorName(),
                req.getActivityId(), req.getBranchId(), req.getDonationPeriod(),
                req.getAmountKhr(), req.getAmountUsd(), req.getExchangeRateKhrPerUsd(),
                req.getPaymentMethodId(), req.getReceiptFileId());

        DonationModel d = DonationModel.builder()
                .donationNo(mintDonationNo())
                .donationTypeId(req.getDonationTypeId())
                .memberId(req.getMemberId())
                .sponsorId(req.getSponsorId())
                .donorName(p.donorName())
                .activityId(req.getActivityId())
                .branchId(req.getBranchId())
                .donationPeriod(req.getDonationPeriod())
                .amountKhr(p.amountKhr())
                .amountUsd(p.amountUsd())
                .exchangeRateKhrPerUsd(req.getExchangeRateKhrPerUsd())
                .totalAmountUsd(p.totalAmountUsd())
                .paymentMethodId(req.getPaymentMethodId())
                .paidAt(req.getPaidAt())
                .paymentReference(normalizeToNull(req.getPaymentReference()))
                .receiptFileId(req.getReceiptFileId())
                .recordedBy(actorId)
                .note(normalizeToNull(req.getNote()))
                .clientRequestId(clientRequestId)
                .build();

        repo.insertDonation(d);
        Long id = d.getId();
        if (id == null) {
            throw new BusinessException("DONATION_INSERT_FAILED", "Failed to persist a donation");
        }

        return new DonationCreateResultDTO(id, d.getDonationNo(), d.getTotalAmountUsd(), d.getCreatedAt());
    }

    // ===================================================================
    // read
    // ===================================================================

    @Transactional(readOnly = true)
    public DonationDTO get(Long id) {
        DonationDTO dto = repo.findById(id);
        if (dto == null) {
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public DonationPageDTO list(Long branchId, Short typeId, Short paymentMethodId,
                                Long memberId, Long sponsorId, Long activityId,
                                OffsetDateTime paidFrom, OffsetDateTime paidTo, String search,
                                int page, int size) {
        int safeSize = Math.clamp(size, 1, 100);
        int safePage = Math.max(page, 0);
        String safeSearch = normalizeToNull(search);

        List<DonationDTO> items = repo.list(
                branchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, safeSearch, safeSize, safePage * safeSize);
        long total = repo.countList(
                branchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, safeSearch);

        return new DonationPageDTO(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public DonationSummaryDTO summary(Long branchId, Short typeId, Short paymentMethodId,
                                      Long memberId, Long sponsorId, Long activityId,
                                      OffsetDateTime paidFrom, OffsetDateTime paidTo, String search) {
        return repo.summary(
                branchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, normalizeToNull(search));
    }

    // ===================================================================
    // update (full replace) + delete
    // ===================================================================

    @Transactional
    public DonationDTO update(Long id, DonationUpdateDTO req) {
        // Fail fast with a clean 404-style code instead of a silent 0-row update.
        if (repo.findById(id) == null) {
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }

        Prepared p = validateAndPrepare(
                req.getDonationTypeId(), req.getMemberId(), req.getSponsorId(), req.getDonorName(),
                req.getActivityId(), req.getBranchId(), req.getDonationPeriod(),
                req.getAmountKhr(), req.getAmountUsd(), req.getExchangeRateKhrPerUsd(),
                req.getPaymentMethodId(), req.getReceiptFileId());

        // donationNo / recordedBy / clientRequestId are intentionally NOT touched
        // by updateDonation's SQL, so leaving them null on the model is safe.
        DonationModel d = DonationModel.builder()
                .id(id)
                .donationTypeId(req.getDonationTypeId())
                .memberId(req.getMemberId())
                .sponsorId(req.getSponsorId())
                .donorName(p.donorName())
                .activityId(req.getActivityId())
                .branchId(req.getBranchId())
                .donationPeriod(req.getDonationPeriod())
                .amountKhr(p.amountKhr())
                .amountUsd(p.amountUsd())
                .exchangeRateKhrPerUsd(req.getExchangeRateKhrPerUsd())
                .totalAmountUsd(p.totalAmountUsd())
                .paymentMethodId(req.getPaymentMethodId())
                .paidAt(req.getPaidAt())
                .paymentReference(normalizeToNull(req.getPaymentReference()))
                .receiptFileId(req.getReceiptFileId())
                .note(normalizeToNull(req.getNote()))
                .build();

        int rows = repo.updateDonation(d);
        if (rows == 0) {
            // Lost a race with a concurrent delete between the existence check and here.
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
        return repo.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        int rows = repo.deleteById(id);
        if (rows == 0) {
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
    }

    // ===================================================================
    // internals
    // ===================================================================

    /** Cleaned + computed values shared by create and update. */
    private record Prepared(String donorName,
                            BigDecimal amountKhr,
                            BigDecimal amountUsd,
                            BigDecimal totalAmountUsd) {
    }

    /**
     * Re-validates every cross-field / referential rule and returns the
     * normalised amounts + the server-computed USD total. Throws
     * {@link BusinessException} with a stable code on the first violation.
     */
    private Prepared validateAndPrepare(Short donationTypeId, Long memberId, Long sponsorId, String donorNameRaw,
                                        Long activityId, Long branchId, LocalDate donationPeriod,
                                        BigDecimal amountKhrRaw, BigDecimal amountUsdRaw, BigDecimal rate,
                                        Short paymentMethodId, Long receiptFileId) {

        // ---- donor source: EXACTLY one of member / sponsor / donorName ----
        String donorName = normalizeToNull(donorNameRaw);
        int sources = (memberId != null ? 1 : 0)
                + (sponsorId != null ? 1 : 0)
                + (donorName != null ? 1 : 0);
        if (sources != 1) {
            throw new BusinessException("DONATION_SOURCE_INVALID",
                    "Exactly one donor source is required: memberId, sponsorId, or donorName");
        }

        // ---- amounts: >= 0 and at least one > 0 ----
        BigDecimal amountKhr = amountKhrRaw != null ? amountKhrRaw : BigDecimal.ZERO;
        BigDecimal amountUsd = amountUsdRaw != null ? amountUsdRaw : BigDecimal.ZERO;
        if (amountKhr.signum() < 0 || amountUsd.signum() < 0) {
            throw new BusinessException("DONATION_AMOUNTS_INVALID", "Amounts must be zero or positive");
        }
        if (amountKhr.signum() == 0 && amountUsd.signum() == 0) {
            throw new BusinessException("DONATION_AMOUNTS_INVALID",
                    "At least one of amountKhr or amountUsd must be greater than zero");
        }

        // ---- exchange rate required when there is a KHR component ----
        if (amountKhr.signum() > 0) {
            if (rate == null) {
                throw new BusinessException("DONATION_EXCHANGE_RATE_REQUIRED",
                        "exchangeRateKhrPerUsd is required when amountKhr is greater than zero");
            }
            if (rate.signum() <= 0) {
                throw new BusinessException("DONATION_EXCHANGE_RATE_INVALID",
                        "exchangeRateKhrPerUsd must be greater than zero");
            }
        }

        // ---- lookups: type + payment method active, branch exists ----
        if (repo.countActiveType(donationTypeId) == 0) {
            throw new BusinessException("DONATION_TYPE_INACTIVE",
                    "Donation type " + donationTypeId + " does not exist or is inactive");
        }
        if (repo.countActivePaymentMethod(paymentMethodId) == 0) {
            throw new BusinessException("DONATION_PAYMENT_METHOD_INACTIVE",
                    "Payment method " + paymentMethodId + " does not exist or is inactive");
        }
        if (repo.countBranch(branchId) == 0) {
            throw new BusinessException("DONATION_BRANCH_NOT_FOUND",
                    "Branch " + branchId + " does not exist");
        }

        // ---- donor source referential checks ----
        if (memberId != null && repo.countMember(memberId) == 0) {
            throw new BusinessException("DONATION_MEMBER_NOT_FOUND",
                    "Member " + memberId + " does not exist");
        }
        if (sponsorId != null && repo.countActiveSponsor(sponsorId) == 0) {
            throw new BusinessException("DONATION_SPONSOR_NOT_FOUND",
                    "Sponsor " + sponsorId + " does not exist or is inactive");
        }

        // ---- optional references ----
        if (activityId != null && repo.countActivity(activityId) == 0) {
            throw new BusinessException("DONATION_ACTIVITY_NOT_FOUND",
                    "Activity " + activityId + " does not exist");
        }
        if (receiptFileId != null && repo.countFile(receiptFileId) == 0) {
            throw new BusinessException("DONATION_RECEIPT_NOT_FOUND",
                    "Receipt file " + receiptFileId + " does not exist");
        }

        // ---- type-specific required fields (per DTO contract) ----
        String typeCode = repo.findTypeCode(donationTypeId);
        if (TYPE_ACTIVITY_DONATION.equals(typeCode) && activityId == null) {
            throw new BusinessException("DONATION_ACTIVITY_REQUIRED",
                    "activityId is required for ACTIVITY_DONATION");
        }
        if (TYPE_MONTHLY_DONATION.equals(typeCode) && donationPeriod == null) {
            throw new BusinessException("DONATION_PERIOD_REQUIRED",
                    "donationPeriod is required for MONTHLY_DONATION");
        }

        BigDecimal totalUsd = computeTotalUsd(amountKhr, amountUsd, rate);
        return new Prepared(donorName, amountKhr, amountUsd, totalUsd);
    }

    /**
     * USD-normalised total: {@code amountUsd + amountKhr / rate}, rounded to 2 dp
     * HALF_UP. The KHR→USD conversion is carried at 6 dp before the final rounding
     * so the last cent is not lost to premature truncation. Precondition (checked
     * by the caller): when {@code amountKhr > 0}, {@code rate} is non-null and &gt; 0.
     */
    private static BigDecimal computeTotalUsd(BigDecimal amountKhr, BigDecimal amountUsd, BigDecimal rate) {
        BigDecimal total = amountUsd;
        if (amountKhr.signum() > 0) {
            BigDecimal khrInUsd = amountKhr.divide(rate, 6, RoundingMode.HALF_UP);
            total = total.add(khrInUsd);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** DON-{yyyyMMdd}-{seq padded to 6}, e.g. DON-20260727-000042. Seq is atomic. */
    private String mintDonationNo() {
        long seq = repo.nextDonationNoSeq();
        String date = DONATION_NO_DATE.format(clock.instant());
        return "DON-" + date + "-" + String.format("%06d", seq);
    }

    /** Rebuilds a create-result for an idempotent replay of an existing donation. */
    private DonationCreateResultDTO buildResult(Long id) {
        DonationDTO d = repo.findById(id);
        if (d == null) {
            // The row backing the idempotency key vanished (e.g. deleted). Treat as
            // not-a-replay rather than returning a dangling result.
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
        return new DonationCreateResultDTO(d.getId(), d.getDonationNo(), d.getTotalAmountUsd(), d.getCreatedAt());
    }

    private static String normalizeToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }
}
