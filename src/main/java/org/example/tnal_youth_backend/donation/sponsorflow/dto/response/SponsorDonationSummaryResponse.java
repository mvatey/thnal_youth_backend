package org.example.tnal_youth_backend.donation.sponsorflow.dto.response;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class SponsorDonationSummaryResponse {
    private long donorCount;
    private BigDecimal totalKhr;
    private BigDecimal totalUsd;
    private BigDecimal overallTotalUsd;
    private BigDecimal donationChangePercent;
    private BigDecimal donorChangePercent;
}
