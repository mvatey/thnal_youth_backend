package org.example.tnal_youth_backend.donation.donation.service;

import org.example.tnal_youth_backend.donation.donation.dto.request.DonationRequest;
import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;

import java.util.List;

public interface DonationService {

    List<DonationResponse> getAllDonations();

    /*
     * Monthly member donations.
     */
    List<DonationResponse> getMonthlyDonations();

    List<DonationResponse> searchMonthlyDonations(
            String period
    );

    List<DonationResponse>
    filterMonthlyDonationsByPaymentMethod(
            Short paymentMethodId
    );

    /*
     * Backward-compatible monthly aliases.
     */
    List<DonationResponse> searchByDonationPeriod(
            String period
    );

    List<DonationResponse> filterByPaymentMethod(
            Short paymentMethodId
    );

    /*
     * Sponsor donations.
     */
    List<DonationResponse> getSponsorDonations();

    List<DonationResponse> searchSponsorDonations(
            String search
    );

    List<DonationResponse>
    filterSponsorDonationsByPaymentMethod(
            Short paymentMethodId
    );

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