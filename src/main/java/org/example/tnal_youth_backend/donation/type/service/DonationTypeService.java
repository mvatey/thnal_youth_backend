package org.example.tnal_youth_backend.donation.type.service;

import org.example.tnal_youth_backend.donation.type.dto.DonationTypeResponse;

import java.util.List;

public interface DonationTypeService {

    List<DonationTypeResponse> getAllDonationTypes(
            Boolean activeOnly
    );

    DonationTypeResponse getDonationTypeById(
            Short id
    );

    DonationTypeResponse getDonationTypeByCode(
            String code
    );
}