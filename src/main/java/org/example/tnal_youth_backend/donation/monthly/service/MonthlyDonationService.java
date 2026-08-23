package org.example.tnal_youth_backend.donation.monthly.service;

import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationBatchRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationUpdateRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationBatchResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationDetailResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationMemberPageResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationPageResponse;

import java.time.LocalDate;

public interface MonthlyDonationService {

    MonthlyDonationMemberPageResponse listMembers(
            Long branchId,
            Integer month,
            Integer year,
            String search,
            int page,
            int size
    );

    MonthlyDonationBatchResponse createBatch(
            MonthlyDonationBatchRequest request
    );

    MonthlyDonationPageResponse listMonthlyDonations(
            Long branchId,
            Integer month,
            Integer year,
            String search,
            int page,
            int size
    );

    MonthlyDonationDetailResponse getMonthlyDonationDetail(
            Long branchId,
            LocalDate donationPeriod
    );

    void updateMonthlyDonation(
            Long donationId,
            MonthlyDonationUpdateRequest request
    );

    void deleteMonthlyDonation(Long donationId);
}