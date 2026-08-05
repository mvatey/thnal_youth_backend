package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class MonthlyDonationDetailResponse {
    private MonthlyDonationBranchResponse branch;
    private LocalDate donationPeriod;
    private MonthlyDonationSummaryResponse summary;
    private List<MonthlyDonationRowResponse> items;
}
