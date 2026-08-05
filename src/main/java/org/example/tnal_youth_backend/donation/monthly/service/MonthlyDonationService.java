package org.example.tnal_youth_backend.donation.monthly.service;

import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationBatchRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.response.*;

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

    void deleteMonthlyDonation(Long donationId);

    MemberMonthlyDonationPageResponse
    listMemberMonthlyDonations(
            Long memberId,
            String search,
            Short paymentMethodId,
            int page,
            int size
    );
}