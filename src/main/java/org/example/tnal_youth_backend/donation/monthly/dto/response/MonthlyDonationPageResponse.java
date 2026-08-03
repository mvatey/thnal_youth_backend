package org.example.tnal_youth_backend.donation.monthly.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MonthlyDonationPageResponse {
    private List<MonthlyDonationListItemResponse> items;
    private long total;
    private int page;
    private int size;
}
