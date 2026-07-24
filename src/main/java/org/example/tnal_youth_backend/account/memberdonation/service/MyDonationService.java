package org.example.tnal_youth_backend.account.memberdonation.service;

import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationSummaryResponse;

import java.util.List;

public interface MyDonationService {

    /*
     * Get every donation directly linked to the
     * currently logged-in member.
     */
    List<MyDonationResponse> getMyDonations();

    /*
     * Get one donation only when it belongs to the
     * currently logged-in member.
     */
    MyDonationResponse getMyDonationById(
            Long donationId
    );

    /*
     * Get donation totals for the logged-in member.
     */
    MyDonationSummaryResponse getMyDonationSummary();
}