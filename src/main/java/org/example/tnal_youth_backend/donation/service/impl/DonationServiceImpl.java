package org.example.tnal_youth_backend.donation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.enums.ActivityBranchRole;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.model.response.ActivityBranchResponse;
import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.service.ActivityInvitedBranchService;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.donation.dto.request.DonationCreateRequest;
import org.example.tnal_youth_backend.donation.dto.response.DonationBranchTotalResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationCreateResultResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationPageResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationSummaryResponse;
import org.example.tnal_youth_backend.donation.dto.request.DonationUpdateRequest;
import org.example.tnal_youth_backend.donation.entity.Donation;
import org.example.tnal_youth_backend.donation.repository.BranchDonationTotalRow;
import org.example.tnal_youth_backend.donation.repository.DonationRepository;
import org.example.tnal_youth_backend.donation.service.DonationService;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Donation recording + reporting service.
 *
 * <p>Written to the SAME conventions as {@code NotificationService}: constructor
 * injection, {@link BusinessException} with a stable machine code for every
 * business rule (so {@code GlobalExceptionHandler} maps it to a 400 with a
 * traceable {@code errorCode}), and {@link SecurityUtils#getCurrentUserId()} for
 * sender/recorder attribution.
 *
 * <p><b>Object-level authorization (branch scoping).</b> Endpoint access is gated
 * at the controller by {@code @PreAuthorize} (STAFF for CRUD, ADMIN for delete),
 * but STAFF is not a single blast radius: a {@code BRANCH_LEADER} may only see and
 * touch donations for THEIR OWN branch (users.branch_id, V14). {@code ADMIN} and
 * {@code SECRETARY} are org-wide. This is enforced here, not in the controller,
 * because it is a row-level rule the annotation cannot express. A branch leader
 * with no branch assigned fails closed (403).
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
 * not required, is normalised to NULL (so a meaningless rate is never stored),
 * and the total is just the USD amount.
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
public class DonationServiceImpl implements DonationService {

    private static final DateTimeFormatter DONATION_NO_DATE =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    /** Donation type codes that carry an extra required field (see DTO javadoc). */
    private static final String TYPE_ACTIVITY_DONATION = "ACTIVITY_DONATION";
    private static final String TYPE_MONTHLY_DONATION = "MONTHLY_DONATION";

    /** Role confined to a single branch. ADMIN / SECRETARY are org-wide. */
    private static final String ROLE_BRANCH_LEADER = "BRANCH_LEADER";

    private final DonationRepository repo;
    private final Clock clock; // utcClock bean from TimeConfig
    private final ActivityRepository activityRepository;
    private final ActivityInvitedBranchRepository activityInvitedBranchRepository;
    private final ActivityInvitedBranchService activityInvitedBranchService;
    private final StaffBranchScopeService staffBranchScopeService;

    // ===================================================================
    // create
    // ===================================================================

    @Transactional
    @Override
    public DonationCreateResultResponse create(DonationCreateRequest req) {
        Long actorId = SecurityUtils.getCurrentUserId();

        enforceStaffBranchAccess(req.getBranchId());

        String clientRequestId = normalizeToNull(req.getClientRequestId());

        // ---- idempotency short-circuit (already-committed replay) ----
        // Scoped to recorded_by = actorId, so a replay can only ever return the
        // caller's own prior donation.
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

        Donation d = Donation.builder()
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
                .exchangeRateKhrPerUsd(p.exchangeRate())
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

        return new DonationCreateResultResponse(id, d.getDonationNo(), d.getTotalAmountUsd(), d.getCreatedAt());
    }

    // ===================================================================
    // read
    // ===================================================================

    @Transactional(readOnly = true)
    @Override
    public DonationResponse get(Long id) {
        DonationResponse dto = repo.findById(id);
        if (dto == null) {
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
        enforceStaffBranchAccess(dto.getBranchId());
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public DonationPageResponse list(Long branchId, Short typeId, Short paymentMethodId,
                                Long memberId, Long sponsorId, Long activityId,
                                OffsetDateTime paidFrom, OffsetDateTime paidTo, String search,
                                int page, int size) {
        int safeSize = Math.clamp(size, 1, 100);
        int safePage = Math.max(page, 0);
        String safeSearch = normalizeSearch(search);
        Long effectiveBranchId = effectiveBranchFilter(branchId);

        List<DonationResponse> items = repo.list(
                effectiveBranchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, safeSearch, safeSize, safePage * safeSize);
        long total = repo.countList(
                effectiveBranchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, safeSearch);

        return new DonationPageResponse(items, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    @Override
    public DonationSummaryResponse summary(Long branchId, Short typeId, Short paymentMethodId,
                                      Long memberId, Long sponsorId, Long activityId,
                                      OffsetDateTime paidFrom, OffsetDateTime paidTo, String search) {
        return repo.summary(
                effectiveBranchFilter(branchId), typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, normalizeSearch(search));
    }

    // ===================================================================
    // update (full replace) + delete
    // ===================================================================

    @Transactional
    @Override
    public DonationResponse update(Long id, DonationUpdateRequest req) {
        Long actorId = SecurityUtils.getCurrentUserId();

        // Fail fast with a clean 404-style code instead of a silent 0-row update.
        DonationResponse current = repo.findById(id);
        if (current == null) {
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }

        enforceStaffBranchAccess(current.getBranchId());
        enforceStaffBranchAccess(req.getBranchId());

        Prepared p = validateAndPrepare(
                req.getDonationTypeId(), req.getMemberId(), req.getSponsorId(), req.getDonorName(),
                req.getActivityId(), req.getBranchId(), req.getDonationPeriod(),
                req.getAmountKhr(), req.getAmountUsd(), req.getExchangeRateKhrPerUsd(),
                req.getPaymentMethodId(), req.getReceiptFileId());

        // donationNo / recordedBy / clientRequestId are intentionally NOT touched
        // by updateDonation's SQL, so leaving them null on the model is safe.
        Donation d = Donation.builder()
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
                .exchangeRateKhrPerUsd(p.exchangeRate())
                .totalAmountUsd(p.totalAmountUsd())
                .paymentMethodId(req.getPaymentMethodId())
                .paidAt(req.getPaidAt())
                .paymentReference(normalizeToNull(req.getPaymentReference()))
                .receiptFileId(req.getReceiptFileId())
                .note(normalizeToNull(req.getNote()))
                .updatedBy(actorId)
                .expectedUpdatedAt(req.getExpectedUpdatedAt())
                .build();

        int rows = repo.updateDonation(d);
        if (rows == 0) {
            // Either the row vanished (concurrent delete) or, when the caller sent an
            // optimistic-lock token, it no longer matches (concurrent edit). Tell the
            // two cases apart so the client knows whether to reload or give up.
            if (req.getExpectedUpdatedAt() != null && repo.findById(id) != null) {
                throw new BusinessException("DONATION_UPDATE_CONFLICT",
                        "Donation " + id + " was modified by someone else; reload and retry");
            }
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
        return repo.findById(id);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        int rows = repo.deleteById(id);
        if (rows == 0) {
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
    }

    // ===================================================================
    // activity donation branch totals
    // ===================================================================

    /**
     * See {@link DonationService#activityBranchTotals}. No new table: the
     * per-branch sums are a plain GROUP BY over the existing {@code donations}
     * rows (repo.sumByActivityGroupedByBranch), merged against the activity's
     * full eligible-branch list (organizer + ACCEPTED co-hosts, from {@link
     * ActivityInvitedBranchService#getActivityBranches}) so a branch that
     * hasn't recorded anything yet still shows up with a zero total instead
     * of being silently missing from the table.
     */
    @Transactional(readOnly = true)
    @Override
    public List<DonationBranchTotalResponse> activityBranchTotals(Long activityId) {
        Long actorId = SecurityUtils.getCurrentUserId();

        // Object-level authz: ADMIN/SECRETARY are org-wide (same as every
        // other read here); a BRANCH_LEADER may only view this if their own
        // branch actually has a stake in the activity — i.e. is the host or
        // has an ACCEPTED co-hosting invitation. Same eligibility rule as
        // recording a donation (validateActivityDonationBranchEligibility),
        // just checked against the viewer's own branch instead of a
        // request's branchId.
        String role = SecurityUtils.getCurrentUserRole();
        java.util.Set<Long> staffScope =
                ("SECRETARY".equals(role) || "BRANCH_LEADER".equals(role))
                        ? staffBranchScopeService.currentStaffBranchIds()
                        : null;

        List<ActivityBranchResponse> eligibleBranches = activityInvitedBranchService
                .getActivityBranches(activityId)
                .stream()
                .filter(branch -> branch.getRole() == ActivityBranchRole.ORGANIZER
                        || branch.getInvitationStatus() == ActivityInvitationStatus.ACCEPTED)
                .filter(branch -> staffScope == null || staffScope.contains(branch.getBranchId()))
                .toList();

        Map<Long, BranchDonationTotalRow> totalsByBranchId = new HashMap<>();
        for (BranchDonationTotalRow row : repo.sumByActivityGroupedByBranch(activityId)) {
            totalsByBranchId.put(row.getBranchId(), row);
        }

        return eligibleBranches.stream()
                .map(branch -> {
                    BranchDonationTotalRow row = totalsByBranchId.get(branch.getBranchId());
                    return DonationBranchTotalResponse.builder()
                            .branchId(branch.getBranchId())
                            .branchCode(branch.getBranchCode())
                            .branchNameKm(branch.getBranchNameKm())
                            .branchNameEn(branch.getBranchNameEn())
                            .role(branch.getRole())
                            .donationCount(row != null && row.getDonationCount() != null ? row.getDonationCount() : 0L)
                            .amountKhr(row != null && row.getAmountKhr() != null ? row.getAmountKhr() : BigDecimal.ZERO)
                            .amountUsd(row != null && row.getAmountUsd() != null ? row.getAmountUsd() : BigDecimal.ZERO)
                            .totalAmountUsd(row != null && row.getTotalAmountUsd() != null ? row.getTotalAmountUsd() : BigDecimal.ZERO)
                            .build();
                })
                .toList();
    }

    // ===================================================================
    // internals
    // ===================================================================

    /** Cleaned + computed values shared by create and update. */
    private record Prepared(String donorName,
                            BigDecimal amountKhr,
                            BigDecimal amountUsd,
                            BigDecimal exchangeRate,
                            BigDecimal totalAmountUsd) {
    }

    /**
     * Re-validates every cross-field / referential rule and returns the
     * normalised amounts, the (normalised) exchange rate and the server-computed
     * USD total. Throws {@link BusinessException} with a stable code on the first
     * violation.
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
        if (TYPE_ACTIVITY_DONATION.equals(typeCode)) {
            // activityId is guaranteed non-null by the check just above.
            validateActivityDonationBranchEligibility(activityId, branchId);
        }
        if (TYPE_MONTHLY_DONATION.equals(typeCode) && donationPeriod == null) {
            throw new BusinessException("DONATION_PERIOD_REQUIRED",
                    "donationPeriod is required for MONTHLY_DONATION");
        }

        // Normalise the rate: it is only meaningful when there is a KHR component.
        // Persisting a rate with a zero-KHR donation would store a misleading number.
        BigDecimal rateOut = amountKhr.signum() > 0 ? rate : null;

        BigDecimal totalUsd = computeTotalUsd(amountKhr, amountUsd, rate);
        return new Prepared(donorName, amountKhr, amountUsd, rateOut, totalUsd);
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
    private DonationCreateResultResponse buildResult(Long id) {
        DonationResponse d = repo.findById(id);
        if (d == null) {
            // The row backing the idempotency key vanished (e.g. deleted). Treat as
            // not-a-replay rather than returning a dangling result.
            throw new BusinessException("DONATION_NOT_FOUND", "Donation " + id + " does not exist");
        }
        return new DonationCreateResultResponse(d.getId(), d.getDonationNo(), d.getTotalAmountUsd(), d.getCreatedAt());
    }

    /**
     * An ACTIVITY_DONATION's branch must be the activity's own host branch,
     * or a branch that has an ACCEPTED invitation to co-host it. This does
     * not change the pre-existing branch scoping above (a BRANCH_LEADER is
     * still confined to their own branch, SECRETARY is still org-wide) — it
     * adds one further, narrower rule: an activity-tied donation may never
     * be attached to a branch that has no relationship to that activity at
     * all, regardless of who is recording it.
     */
    private void validateActivityDonationBranchEligibility(Long activityId, Long branchId) {
        if (!isBranchEligibleForActivity(activityId, branchId)) {
            throw new BusinessException("DONATION_BRANCH_NOT_ELIGIBLE",
                    "Branch " + branchId + " is not this activity's host branch and has not "
                            + "accepted an invitation to it");
        }
    }

    /**
     * True when {@code branchId} is this activity's own host branch, or has
     * an ACCEPTED invitation to co-host it. Shared by the donation-record
     * eligibility check above and {@link #activityBranchTotals}'s
     * view-access check below — both boil down to the same "does this
     * branch actually have a stake in this activity" question, just applied
     * to a request's branchId in one case and the viewer's own branch in
     * the other.
     */
    private boolean isBranchEligibleForActivity(Long activityId, Long branchId) {
        Long hostBranchId = activityRepository.findById(activityId)
                .map(Activity::getBranchId)
                .orElse(null);

        if (hostBranchId != null && hostBranchId.equals(branchId)) {
            return true;
        }

        return activityInvitedBranchRepository
                .findByActivity_IdAndBranch_IdAndInvitationStatus(
                        activityId, branchId, ActivityInvitationStatus.ACCEPTED)
                .isPresent();
    }

    /**
     * The branch this actor is confined to, or {@code null} for org-wide roles
     * (ADMIN / SECRETARY, or any non-branch-leader). A BRANCH_LEADER with no branch
     * assigned fails closed with 403 — we will not silently widen their scope.
     */
    private void enforceStaffBranchAccess(Long branchId) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!"SECRETARY".equals(role) && !"BRANCH_LEADER".equals(role)) {
            return;
        }
        if (!staffBranchScopeService.currentStaffBranchIds().contains(branchId)) {
            throw new AccessDeniedException(
                    "This branch is outside your permitted scope"
            );
        }
    }

    private Long effectiveBranchFilter(Long requestedBranchId) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!"SECRETARY".equals(role) && !"BRANCH_LEADER".equals(role)) {
            return requestedBranchId;
        }

        var allowed = staffBranchScopeService.currentStaffBranchIds();
        if (requestedBranchId == null) {
            if (allowed.size() == 1) {
                return allowed.iterator().next();
            }
            throw new AccessDeniedException(
                    "Select one of your assigned branches"
            );
        }

        if (!allowed.contains(requestedBranchId)) {
            throw new AccessDeniedException(
                    "This branch is outside your permitted scope"
            );
        }
        return requestedBranchId;
    }

    private static String normalizeToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }

    /**
     * Normalise a free-text search term and escape LIKE/ILIKE metacharacters so
     * user input is matched literally. PostgreSQL uses backslash as the default
     * LIKE escape character, so escaping {@code \ % _} here is sufficient without
     * an explicit ESCAPE clause. Blank → null (no filter).
     */
    private static String normalizeSearch(String s) {
        String v = normalizeToNull(s);
        if (v == null) {
            return null;
        }
        return v.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
