package org.example.tnal_youth_backend.member.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivityDonationSummaryResponse {

    private long donationCount;
    private BigDecimal totalDonationKhr;
    private BigDecimal totalDonationUsd;
    private long materialDonationCount;
    private long bankPaymentCount;
}
