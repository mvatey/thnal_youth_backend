package org.example.tnal_youth_backend.donation.sponsorflow.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
@Data @AllArgsConstructor
public class SponsorDonationPageResponse {
    private List<SponsorDonationRowResponse> items;
    private long total;
    private int page;
    private int size;
}
