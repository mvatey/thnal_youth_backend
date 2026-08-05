package org.example.tnal_youth_backend.donation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Aggregate totals for a filtered set of donations (dashboard / reporting).
 *
 * <p>Sums are computed in SQL over the SAME filter set as the list endpoint, so
 * "total shown" and "total summed" always agree. USD is the normalised figure to
 * lean on ({@code sumTotalUsd}); the raw KHR/USD sums are kept for transparency.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationSummaryResponse {
    /** Number of donations matching the filters. */
    private long count;
    /** Sum of total_amount_usd (USD-normalised grand total). */
    private BigDecimal sumTotalUsd;
    /** Sum of the raw amount_khr column. */
    private BigDecimal sumAmountKhr;
    /** Sum of the raw amount_usd column. */
    private BigDecimal sumAmountUsd;
}
