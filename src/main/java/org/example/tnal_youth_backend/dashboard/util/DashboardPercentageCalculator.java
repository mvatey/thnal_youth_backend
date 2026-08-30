package org.example.tnal_youth_backend.dashboard.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DashboardPercentageCalculator {

    private static final BigDecimal ONE_HUNDRED =
            BigDecimal.valueOf(100);

    public BigDecimal calculate(
            long current,
            long previous
    ) {
        return calculate(
                BigDecimal.valueOf(current),
                BigDecimal.valueOf(previous)
        );
    }

    public BigDecimal calculate(
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

        /*
         * A zero previous value has no true ratio to divide by --
         * treat any positive current value as full (100%) growth
         * rather than leaving it undefined, so a brand new count
         * still shows as growing instead of stuck at "no change".
         */
        if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
            return safeCurrent.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : ONE_HUNDRED;
        }

        return safeCurrent
                .subtract(safePrevious)
                .multiply(ONE_HUNDRED)
                .divide(
                        safePrevious,
                        2,
                        RoundingMode.HALF_UP
                );
    }
}