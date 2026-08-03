package org.example.tnal_youth_backend.donation.sponsorflow.dto.response;
import lombok.Data;
@Data
public class SponsorLookupResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String donorKind;
}
