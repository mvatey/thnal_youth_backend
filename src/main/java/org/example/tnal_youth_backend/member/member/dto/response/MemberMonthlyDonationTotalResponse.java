package org.example.tnal_youth_backend.member.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberMonthlyDonationTotalResponse {

    private BigDecimal totalKhr;

    private BigDecimal totalUsd;
}