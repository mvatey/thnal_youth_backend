package org.example.tnal_youth_backend.account.memberdonation.service;

import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationSummaryResponse;

import java.util.List;

public interface MyDonationService {

    List<MyDonationResponse> getMyDonations();

    List<MyDonationResponse> searchByDonationPeriod(
            String period
    );

    List<MyDonationResponse> filterByPaymentMethod(
            Short paymentMethodId
    );

    MyDonationResponse getMyDonationById(
            Long donationId
    );

    MyDonationSummaryResponse getMyDonationSummary();
}