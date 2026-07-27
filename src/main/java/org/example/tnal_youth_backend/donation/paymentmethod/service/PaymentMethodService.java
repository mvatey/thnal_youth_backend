package org.example.tnal_youth_backend.donation.paymentmethod.service;

import org.example.tnal_youth_backend.donation.paymentmethod.dto.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {

    List<PaymentMethodResponse> getAllPaymentMethods(
            Boolean activeOnly
    );

    PaymentMethodResponse getPaymentMethodById(
            Short id
    );

    PaymentMethodResponse getPaymentMethodByCode(
            String code
    );
}