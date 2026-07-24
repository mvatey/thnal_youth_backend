package org.example.tnal_youth_backend.donation.sponsor.service;

import org.example.tnal_youth_backend.donation.sponsor.dto.request.SponsorRequest;
import org.example.tnal_youth_backend.donation.sponsor.dto.response.SponsorResponse;

import java.util.List;

public interface SponsorService {

    List<SponsorResponse> getAllSponsors();

    List<SponsorResponse> getActiveSponsors();

    SponsorResponse getSponsorById(
            Long id
    );

    SponsorResponse createSponsor(
            SponsorRequest request
    );

    SponsorResponse updateSponsor(
            Long id,
            SponsorRequest request
    );

    void deleteSponsor(
            Long id
    );
}