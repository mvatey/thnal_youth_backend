package org.example.tnal_youth_backend.dashboard.util;

import org.example.tnal_youth_backend.dashboard.exception.InvalidDashboardMonthException;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.ZoneId;

@Component
public class DashboardMonthResolver {

    private static final ZoneId CAMBODIA_ZONE =
            ZoneId.of("Asia/Phnom_Penh");

    private static final String MONTH_PATTERN =
            "^\\d{4}-(0[1-9]|1[0-2])$";

    public DashboardMonthRange resolve(String month) {

        if (month == null || month.isBlank()) {
            return DashboardMonthRange.from(
                    YearMonth.now(CAMBODIA_ZONE)
            );
        }

        String value = month.trim();

        if (!value.matches(MONTH_PATTERN)) {
            throw invalidMonth();
        }

        try {
            return DashboardMonthRange.from(
                    YearMonth.parse(value)
            );
        } catch (DateTimeException exception) {
            throw invalidMonth();
        }
    }

    private InvalidDashboardMonthException invalidMonth() {
        return new InvalidDashboardMonthException(
                "Invalid month format. Expected yyyy-MM, for example 2026-07."
        );
    }
}