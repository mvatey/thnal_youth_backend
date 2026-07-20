package org.example.tnal_youth_backend.donation.type.mapper;

import org.example.tnal_youth_backend.donation.type.dto.DonationTypeResponse;
import org.example.tnal_youth_backend.donation.type.model.entity.DonationType;
import org.springframework.stereotype.Component;

@Component
public class DonationTypeMapper {

    public DonationTypeResponse toResponse(
            DonationType donationType
    ) {

        if (donationType == null) {
            return null;
        }

        return new DonationTypeResponse(
                donationType.getId(),
                donationType.getCode(),
                donationType.getLabelKm(),
                donationType.getLabelEn(),
                donationType.getDescription(),
                donationType.getIsActive(),
                donationType.getSortOrder(),
                donationType.getCreatedAt()
        );
    }
}