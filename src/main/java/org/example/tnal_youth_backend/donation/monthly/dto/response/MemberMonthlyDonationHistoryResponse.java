package org.example.tnal_youth_backend.donation.monthly.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MemberMonthlyDonationHistoryResponse(
        List<MemberMonthlyDonationResponse> content,
        long totalElements,
        BigDecimal totalKhr,
        BigDecimal totalUsd,
        BigDecimal overallTotalUsd,
        long cashCount,
        long bankCount
) {
}
