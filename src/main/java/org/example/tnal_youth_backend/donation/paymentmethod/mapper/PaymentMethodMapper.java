package org.example.tnal_youth_backend.donation.paymentmethod.mapper;

import org.example.tnal_youth_backend.donation.paymentmethod.dto.PaymentMethodResponse;
import org.example.tnal_youth_backend.donation.paymentmethod.model.entity.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodMapper {

    public PaymentMethodResponse toResponse(
            PaymentMethod paymentMethod
    ) {

        if (paymentMethod == null) {
            return null;
        }

        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getCode(),
                paymentMethod.getLabelKm(),
                paymentMethod.getLabelEn(),
                paymentMethod.getDescription(),
                paymentMethod.getIsActive(),
                paymentMethod.getSortOrder(),
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt()
        );
    }
}