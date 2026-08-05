package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.Data;

@Data
public class MonthlyDonationBranchResponse {
    private Long id;
    private String branchCode;
    private String nameKm;
    private String nameEn;
}
