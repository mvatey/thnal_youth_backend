package org.example.tnal_youth_backend.donation.service;

import org.example.tnal_youth_backend.donation.dto.request.DonationCreateRequest;
import org.example.tnal_youth_backend.donation.dto.request.DonationUpdateRequest;
import org.example.tnal_youth_backend.donation.dto.response.DonationBranchTotalResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationCreateResultResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationPageResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationSummaryResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface DonationService {

    DonationCreateResultResponse create(DonationCreateRequest request);

    DonationResponse get(Long id);

    DonationPageResponse list(
            Long branchId,
            Short typeId,
            Short paymentMethodId,
            Long memberId,
            Long sponsorId,
            Long activityId,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo,
            String search,
            int page,
            int size
    );

    DonationSummaryResponse summary(
            Long branchId,
            Short typeId,
            Short paymentMethodId,
            Long memberId,
            Long sponsorId,
            Long activityId,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo,
            String search
    );

    DonationResponse update(Long id, DonationUpdateRequest request);

    void delete(Long id);

    /**
     * Every branch eligible to record a donation for this activity (the
     * organizer plus every ACCEPTED co-hosting branch), each with its
     * running total — see {@link DonationBranchTotalResponse}. Lets the
     * organizer (or any accepted co-host) see how much every OTHER
     * participating branch has raised for this one activity, without
     * exposing their individual donation rows.
     */
    List<DonationBranchTotalResponse> activityBranchTotals(Long activityId);

    /**
     * The activity's grand total across every donation type (member/branch
     * donations plus sponsor donations earmarked for it) — the figure for
     * the top-level summary cards, as opposed to {@link #activityBranchTotals}'s
     * per-branch, member-donation-only breakdown.
     */
    DonationSummaryResponse activityDonationTotal(Long activityId);
}
