package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class MonthlyDonationSavedItemResponse {
    private Long donationId;
    private String donationNo;
    private Long memberId;
    private BigDecimal totalAmountUsd;
    private OffsetDateTime createdAt;
}
