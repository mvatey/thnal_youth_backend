package org.example.tnal_youth_backend.donation.repository;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Raw MyBatis result row for {@link DonationRepository#sumByActivityGroupedByBranch}
 * — one row per branch_id that has at least one donation recorded against a
 * given activity. NOT an API response type (see
 * {@code donation.dto.response.DonationBranchTotalResponse} for that) — this
 * is only the SQL-shaped carrier the service merges against the activity's
 * full eligible-branch list (host + accepted co-hosts), since a branch that
 * hasn't recorded anything yet has no row here at all.
 */
@Data
@NoArgsConstructor
public class BranchDonationTotalRow {
    private Long branchId;
    private Long donationCount;
    /** Raw SUM(amount_khr) — the riel component, unconverted. */
    private BigDecimal amountKhr;
    /** Raw SUM(amount_usd) — the direct-USD component, unconverted. */
    private BigDecimal amountUsd;
    /** SUM(total_amount_usd) — every donation's USD-normalised total. */
    private BigDecimal totalAmountUsd;
}
