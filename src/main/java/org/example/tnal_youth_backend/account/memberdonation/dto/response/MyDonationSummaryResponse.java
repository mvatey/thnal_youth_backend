package org.example.tnal_youth_backend.account.memberdonation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyDonationSummaryResponse(

        /*
         * ======================================================
         * ALL DONATIONS
         * ======================================================
         */

        long totalDonationRecords,

        BigDecimal totalAmountKhr,

        BigDecimal totalAmountUsd,

        BigDecimal overallTotalUsd,

        /*
         * ======================================================
         * MONTHLY DONATIONS
         * ======================================================
         */

        long monthlyDonationRecords,

        BigDecimal monthlyAmountKhr,

        BigDecimal monthlyAmountUsd,

        BigDecimal monthlyOverallTotalUsd,

        /*
         * ======================================================
         * SPONSOR DONATIONS
         * ======================================================
         */

        long sponsorDonationRecords,

        BigDecimal sponsorAmountKhr,

        BigDecimal sponsorAmountUsd,

        BigDecimal sponsorOverallTotalUsd,

        /*
         * ======================================================
         * ACTIVITY DONATIONS
         * ======================================================
         */

        long activityDonationRecords,

        BigDecimal activityAmountKhr,

        BigDecimal activityAmountUsd,

        BigDecimal activityOverallTotalUsd,

        /*
         * ======================================================
         * LATEST TRANSACTION
         * ======================================================
         */

        OffsetDateTime latestPaidAt

) {
}