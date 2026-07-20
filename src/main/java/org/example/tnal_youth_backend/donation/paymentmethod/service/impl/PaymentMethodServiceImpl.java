package org.example.tnal_youth_backend.donation.paymentmethod.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.paymentmethod.dto.PaymentMethodResponse;
import org.example.tnal_youth_backend.donation.paymentmethod.mapper.PaymentMethodMapper;
import org.example.tnal_youth_backend.donation.paymentmethod.model.entity.PaymentMethod;
import org.example.tnal_youth_backend.donation.paymentmethod.repository.PaymentMethodRepository;
import org.example.tnal_youth_backend.donation.paymentmethod.service.PaymentMethodService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodServiceImpl
        implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    public List<PaymentMethodResponse> getAllPaymentMethods(
            Boolean activeOnly
    ) {

        List<PaymentMethod> paymentMethods;

        if (Boolean.TRUE.equals(activeOnly)) {
            paymentMethods =
                    paymentMethodRepository
                            .findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
        } else {
            paymentMethods =
                    paymentMethodRepository
                            .findAllByOrderBySortOrderAscIdAsc();
        }

        return paymentMethods.stream()
                .map(paymentMethodMapper::toResponse)
                .toList();
    }

    @Override
    public PaymentMethodResponse getPaymentMethodById(
            Short id
    ) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment method id must be greater than zero"
            );
        }

        PaymentMethod paymentMethod =
                paymentMethodRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment method not found with id: " + id
                                )
                        );

        return paymentMethodMapper.toResponse(paymentMethod);
    }

    @Override
    public PaymentMethodResponse getPaymentMethodByCode(
            String code
    ) {

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment method code is required"
            );
        }

        String normalizedCode = code.trim();

        PaymentMethod paymentMethod =
                paymentMethodRepository
                        .findByCodeIgnoreCase(normalizedCode)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Payment method not found with code: "
                                                + normalizedCode
                                )
                        );

        return paymentMethodMapper.toResponse(paymentMethod);
    }
}