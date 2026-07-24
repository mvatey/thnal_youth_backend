package org.example.tnal_youth_backend.donation.donation.service;

import org.example.tnal_youth_backend.donation.donation.dto.request.DonationRequest;
import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;

import java.util.List;

public interface DonationService {

    List<DonationResponse> getAllDonations();

    DonationResponse getDonationById(
            Long id
    );

    DonationResponse createDonation(
            DonationRequest request
    );

    DonationResponse updateDonation(
            Long id,
            DonationRequest request
    );

    void deleteDonation(
            Long id
    );
}