package org.example.tnal_youth_backend.account.memberdonation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MyDonationSummaryResponse(

        long totalDonationRecords,

        BigDecimal totalAmountKhr,

        BigDecimal totalAmountUsd,

        BigDecimal overallTotalUsd,

        long monthlyDonationRecords,

        long activityDonationRecords,

        long sponsorDonationRecords,

        OffsetDateTime latestPaidAt
) {
}