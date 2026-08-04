package org.example.tnal_youth_backend.donation.sponsorflow.service;

import org.example.tnal_youth_backend.donation.sponsorflow.dto.request.SponsorDonationUpsertRequest;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationPageResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationRowResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorLookupResponse;

import java.time.LocalDate;
import java.util.List;

public interface SponsorDonationService {

    SponsorDonationRowResponse create(SponsorDonationUpsertRequest request);

    SponsorDonationRowResponse update(Long donationId, SponsorDonationUpsertRequest request);

    SponsorDonationPageResponse list(
            Long branchId,
            String donorKind,
            LocalDate paidDate,
            String search,
            int page,
            int size
    );

    SponsorDonationRowResponse get(Long donationId);

    void delete(Long donationId);

    SponsorDonationSummaryResponse summary(Long branchId, LocalDate paidDate);

    List<SponsorLookupResponse> sponsors(String search);

    List<SponsorLookupResponse> members(Long branchId, String search);
}
