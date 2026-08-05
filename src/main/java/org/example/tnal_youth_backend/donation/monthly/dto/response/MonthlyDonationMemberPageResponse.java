package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MonthlyDonationMemberPageResponse {
    private Long branchId;
    private LocalDate donationPeriod;
    private List<MonthlyDonationMemberResponse> items;
    private long total;
    private int page;
    private int size;
}
