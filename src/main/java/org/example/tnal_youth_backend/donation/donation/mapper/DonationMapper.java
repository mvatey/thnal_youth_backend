package org.example.tnal_youth_backend.donation.donation.mapper;

import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.donation.entity.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    public DonationResponse toResponse(
            Donation donation
    ) {
        if (donation == null) {
            return null;
        }

        return new DonationResponse(
                donation.getId(),
                donation.getDonationNo(),
                donation.getDonationTypeId(),
                donation.getMemberId(),
                donation.getSponsorId(),
                donation.getDonorName(),
                donation.getActivityId(),
                donation.getBranchId(),
                donation.getDonationPeriod(),
                donation.getAmountKhr(),
                donation.getAmountUsd(),
                donation.getExchangeRateKhrPerUsd(),
                donation.getTotalAmountUsd(),
                donation.getPaymentMethodId(),
                donation.getPaidAt(),
                donation.getPaymentReference(),
                donation.getReceiptFileId(),
                donation.getNote()
        );
    }
}