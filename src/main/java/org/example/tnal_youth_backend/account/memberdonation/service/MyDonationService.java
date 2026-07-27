package org.example.tnal_youth_backend.account.memberdonation.service;

import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;

import java.util.List;

public interface MyDonationService {

    /*
     * Monthly donations
     */
    List<MyDonationResponse> getMyMonthlyDonations();

    List<MyDonationResponse> searchMyMonthlyDonations(
            String period
    );

    List<MyDonationResponse>
    filterMyMonthlyDonationsByPaymentMethod(
            Short paymentMethodId
    );

    /*
     * Sponsor donations
     */
    List<MyDonationResponse> getMySponsorDonations();

    List<MyDonationResponse> searchMySponsorDonations(
            String search
    );

    List<MyDonationResponse>
    filterMySponsorDonationsByPaymentMethod(
            Short paymentMethodId
    );
}