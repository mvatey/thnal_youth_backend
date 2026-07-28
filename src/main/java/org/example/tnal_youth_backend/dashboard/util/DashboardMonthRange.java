package org.example.tnal_youth_backend.dashboard.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public record DashboardMonthRange(

        YearMonth selectedMonth,

        LocalDate previousMonthStartDate,
        LocalDate selectedMonthStartDate,
        LocalDate nextMonthStartDate,

        OffsetDateTime previousMonthStart,
        OffsetDateTime selectedMonthStart,
        OffsetDateTime nextMonthStart

) {

    private static final ZoneId CAMBODIA_ZONE =
            ZoneId.of("Asia/Phnom_Penh");

    public static DashboardMonthRange from(YearMonth month) {

        LocalDate previousStartDate =
                month.minusMonths(1).atDay(1);

        LocalDate selectedStartDate =
                month.atDay(1);

        LocalDate nextStartDate =
                month.plusMonths(1).atDay(1);

        return new DashboardMonthRange(
                month,

                previousStartDate,
                selectedStartDate,
                nextStartDate,

                previousStartDate
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime(),

                selectedStartDate
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime(),

                nextStartDate
                        .atStartOfDay(CAMBODIA_ZONE)
                        .toOffsetDateTime()
        );
    }

    public String period() {
        return selectedMonth.toString();
    }
}