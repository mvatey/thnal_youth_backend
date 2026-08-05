package org.example.tnal_youth_backend.donation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Paginated donation list with a total for UI page counts. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationPageResponse {
    private List<DonationResponse> items;
    private long total;
    private int page;
    private int size;
}
