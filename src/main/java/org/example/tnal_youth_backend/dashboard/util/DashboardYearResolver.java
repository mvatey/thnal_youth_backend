package org.example.tnal_youth_backend.dashboard.util;

import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.Year;
import java.time.ZoneId;

@Component
public class DashboardYearResolver {

    private static final ZoneId CAMBODIA_ZONE =
            ZoneId.of("Asia/Phnom_Penh");

    public int resolve(Integer year) {
        if (year == null) {
            return Year.now(CAMBODIA_ZONE).getValue();
        }

        try {
            return Year.of(year).getValue();
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    "Invalid dashboard year."
            );
        }
    }
}