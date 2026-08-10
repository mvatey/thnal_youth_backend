package org.example.tnal_youth_backend.activity.income.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MemberActivityIncomeHistoryResponse(
        List<MemberActivityIncomeResponse> content,
        long totalElements,
        long activityCount,
        BigDecimal totalKhr,
        BigDecimal totalUsd,
        BigDecimal overallTotalUsd,
        long materialCount,
        long bankCount
) {
}
