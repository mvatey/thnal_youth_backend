package org.example.tnal_youth_backend.dashboard.repository.projection;

import java.math.BigDecimal;

public record DonationTotals(
        BigDecimal amountKhr,
        BigDecimal amountUsd
) {

    public static DonationTotals zero() {
        return new DonationTotals(
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}