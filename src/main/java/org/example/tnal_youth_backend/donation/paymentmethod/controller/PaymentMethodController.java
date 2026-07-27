package org.example.tnal_youth_backend.donation.paymentmethod.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.paymentmethod.dto.PaymentMethodResponse;
import org.example.tnal_youth_backend.donation.paymentmethod.service.PaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(
        name = "C. Member Page - Payment-methods"
)
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>>
    getAllPaymentMethods(

            @RequestParam(
                    name = "activeOnly",
                    defaultValue = "false"
            )
            Boolean activeOnly
    ) {

        return ResponseEntity.ok(
                paymentMethodService
                        .getAllPaymentMethods(activeOnly)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse>
    getPaymentMethodById(

            @PathVariable
            Short id
    ) {

        return ResponseEntity.ok(
                paymentMethodService.getPaymentMethodById(id)
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PaymentMethodResponse>
    getPaymentMethodByCode(

            @PathVariable
            String code
    ) {

        return ResponseEntity.ok(
                paymentMethodService
                        .getPaymentMethodByCode(code)
        );
    }
}