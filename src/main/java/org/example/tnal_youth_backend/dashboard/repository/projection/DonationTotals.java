package org.example.tnal_youth_backend.dashboard.repository.projection;

import java.math.BigDecimal;

/**
 * USD-normalised donation total ({@code SUM(donations.total_amount_usd)}) --
 * the same figure the rest of the donation module already treats as the
 * canonical grand total (branch totals, sponsor flow, monthly summaries).
 * Summing {@code amount_khr} and {@code amount_usd} as two separate pots
 * instead would silently drop KHR-denominated donations from a USD-only
 * display, which is exactly the bug this replaced.
 */
public record DonationTotals(
        BigDecimal totalAmountUsd
) {

    public static DonationTotals zero() {
        return new DonationTotals(
                BigDecimal.ZERO
        );
    }
}
