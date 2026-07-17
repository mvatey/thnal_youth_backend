package org.example.tnal_youth_backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record ActivityBreakdownResponse(
        String period,
        Long total,
        List<ActivityTypeItem> items
) {

    public record ActivityTypeItem(
            String code,
            String labelKm,
            String labelEn,
            Long count,
            BigDecimal percentage
    ) {
    }
}