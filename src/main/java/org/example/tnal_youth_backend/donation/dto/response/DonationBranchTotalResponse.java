package org.example.tnal_youth_backend.donation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.model.enums.ActivityBranchRole;

import java.math.BigDecimal;

/**
 * One row of {@code GET /api/donations/activity/{activityId}/branch-totals}
 * — every branch eligible to record a donation for this activity (the
 * organizer, plus every branch with an ACCEPTED co-hosting invitation),
 * each with its running total. A branch that hasn't recorded anything yet
 * still gets a row here, with {@code totalAmountUsd} = 0 — this mirrors the
 * activity/{id}/members invite table always listing every roster member
 * rather than only the ones already invited.
 *
 * <p>This is deliberately an AGGREGATE-ONLY view: it never exposes another
 * branch's individual donation rows (donor name, member, payment method,
 * etc.) — only the summed total and count. A branch's own itemised
 * donations stay visible only to that branch's own staff (or org-wide
 * ADMIN/SECRETARY) via the existing {@code GET /api/donations} list, which
 * keeps its normal own-branch scoping unchanged. Reused for every viewer
 * (host or any accepted co-host) so every branch sees the SAME totals for
 * this activity — there is nothing branch-specific to filter here once
 * access is granted (see DonationServiceImpl#activityBranchTotals for the
 * access check itself).
 *
 * <p>Plain camelCase fields, no {@code @JsonProperty} overrides — matching
 * every other DTO in {@code donation.dto.response} (DonationResponse,
 * DonationSummaryResponse, ...), all of which serialize as camelCase JSON.
 * (The activity module's {@code ActivityBranchResponse}, which this reuses
 * {@link ActivityBranchRole} from, happens to use snake_case
 * {@code @JsonProperty} overrides instead — that is that module's own
 * convention, not a project-wide one, so it is deliberately not copied
 * here.)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationBranchTotalResponse {

    private Long branchId;
    private String branchCode;
    private String branchNameKm;
    private String branchNameEn;

    private ActivityBranchRole role;

    private long donationCount;
    /** Raw SUM(amount_khr) for this branch — the riel component, unconverted. */
    private BigDecimal amountKhr;
    /** Raw SUM(amount_usd) for this branch — the direct-USD component, unconverted. */
    private BigDecimal amountUsd;
    /** SUM(total_amount_usd) — every one of this branch's donations, USD-normalised. */
    private BigDecimal totalAmountUsd;
}
