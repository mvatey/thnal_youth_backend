package org.example.tnal_youth_backend.donation.type.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.type.dto.DonationTypeResponse;
import org.example.tnal_youth_backend.donation.type.mapper.DonationTypeMapper;
import org.example.tnal_youth_backend.donation.type.model.entity.DonationType;
import org.example.tnal_youth_backend.donation.type.repository.DonationTypeRepository;
import org.example.tnal_youth_backend.donation.type.service.DonationTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationTypeServiceImpl
        implements DonationTypeService {

    private final DonationTypeRepository donationTypeRepository;
    private final DonationTypeMapper donationTypeMapper;

    @Override
    public List<DonationTypeResponse> getAllDonationTypes(
            Boolean activeOnly
    ) {

        List<DonationType> donationTypes;

        if (Boolean.TRUE.equals(activeOnly)) {
            donationTypes =
                    donationTypeRepository
                            .findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
        } else {
            donationTypes =
                    donationTypeRepository
                            .findAllByOrderBySortOrderAscIdAsc();
        }

        return donationTypes
                .stream()
                .map(donationTypeMapper::toResponse)
                .toList();
    }

    @Override
    public DonationTypeResponse getDonationTypeById(
            Short id
    ) {

        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation type id is required"
            );
        }

        DonationType donationType =
                donationTypeRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Donation type not found with id: " + id
                                )
                        );

        return donationTypeMapper.toResponse(
                donationType
        );
    }

    @Override
    public DonationTypeResponse getDonationTypeByCode(
            String code
    ) {

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation type code is required"
            );
        }

        String normalizedCode =
                code.trim();

        DonationType donationType =
                donationTypeRepository
                        .findByCodeIgnoreCase(
                                normalizedCode
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Donation type not found with code: "
                                                + normalizedCode
                                )
                        );

        return donationTypeMapper.toResponse(
                donationType
        );
    }
}