package org.example.tnal_youth_backend.donation.donation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.donation.dto.request.DonationRequest;
import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.donation.service.DonationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Donations",
        description = "Manage member donation records"
)
public class DonationController {

    private final DonationService donationService;

    /*
     * Normal GET endpoint.
     *
     * GET /api/donations
     */
    @GetMapping
    public ResponseEntity<List<DonationResponse>>
    getAllDonations() {

        return ResponseEntity.ok(
                donationService.getAllDonations()
        );
    }

    /*
     * Search donations by monthly donation period.
     *
     * Required format:
     * yyyy-MM
     *
     * Example:
     * GET /api/donations/search?period=2026-07
     */
    @GetMapping("/search")
    public ResponseEntity<List<DonationResponse>>
    searchByDonationPeriod(

            @RequestParam
            String period
    ) {
        return ResponseEntity.ok(
                donationService.searchByDonationPeriod(
                        period
                )
        );
    }

    /*
     * Filter donations by payment method.
     *
     * Example:
     * GET /api/donations/filter/payment-method?paymentMethodId=1
     */
    @GetMapping("/filter/payment-method")
    public ResponseEntity<List<DonationResponse>>
    filterByPaymentMethod(

            @RequestParam
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                donationService.filterByPaymentMethod(
                        paymentMethodId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse>
    getDonationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                donationService.getDonationById(
                        id
                )
        );
    }

    @PostMapping
    public ResponseEntity<DonationResponse>
    createDonation(

            @Valid
            @RequestBody
            DonationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        donationService.createDonation(
                                request
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonationResponse>
    updateDonation(

            @PathVariable Long id,

            @Valid
            @RequestBody
            DonationRequest request
    ) {
        return ResponseEntity.ok(
                donationService.updateDonation(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteDonation(
            @PathVariable Long id
    ) {
        donationService.deleteDonation(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}