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

//@RestController
//@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Donations",
        description = "Manage monthly member donations and sponsor donations"
)
public class LegacyMemberDonationController {

    private final DonationService donationService;

    /*
     * All donation transactions.
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
     * ==========================================================
     * MONTHLY MEMBER DONATIONS
     * ==========================================================
     */

    /*
     * GET /api/donations/monthly
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<DonationResponse>>
    getMonthlyDonations() {

        return ResponseEntity.ok(
                donationService.getMonthlyDonations()
        );
    }

    /*
     * GET /api/donations/monthly/search?period=2026-07
     */
    @GetMapping("/monthly/search")
    public ResponseEntity<List<DonationResponse>>
    searchMonthlyDonations(
            @RequestParam
            String period
    ) {
        return ResponseEntity.ok(
                donationService.searchMonthlyDonations(
                        period
                )
        );
    }

    /*
     * GET /api/donations/monthly/filter/payment-method
     *     ?paymentMethodId=1
     */
    @GetMapping("/monthly/filter/payment-method")
    public ResponseEntity<List<DonationResponse>>
    filterMonthlyDonationsByPaymentMethod(
            @RequestParam
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                donationService
                        .filterMonthlyDonationsByPaymentMethod(
                                paymentMethodId
                        )
        );
    }

    /*
     * Backward-compatible monthly search endpoint.
     *
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
     * Backward-compatible monthly payment-method filter.
     *
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

    /*
     * ==========================================================
     * SPONSOR DONATIONS
     * ==========================================================
     */

    /*
     * GET /api/donations/sponsors
     */
    @GetMapping("/sponsors")
    public ResponseEntity<List<DonationResponse>>
    getSponsorDonations() {

        return ResponseEntity.ok(
                donationService.getSponsorDonations()
        );
    }

    /*
     * Searches registered sponsor name, phone, or email.
     *
     * GET /api/donations/sponsors/search?search=វិសាល
     */
    @GetMapping("/sponsors/search")
    public ResponseEntity<List<DonationResponse>>
    searchSponsorDonations(
            @RequestParam
            String search
    ) {
        return ResponseEntity.ok(
                donationService.searchSponsorDonations(
                        search
                )
        );
    }

    /*
     * GET /api/donations/sponsors/filter/payment-method
     *     ?paymentMethodId=1
     */
    @GetMapping("/sponsors/filter/payment-method")
    public ResponseEntity<List<DonationResponse>>
    filterSponsorDonationsByPaymentMethod(
            @RequestParam
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                donationService
                        .filterSponsorDonationsByPaymentMethod(
                                paymentMethodId
                        )
        );
    }

    /*
     * ==========================================================
     * STANDARD CRUD
     * ==========================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<DonationResponse>
    getDonationById(
            @PathVariable
            Long id
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
            @PathVariable
            Long id,

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
            @PathVariable
            Long id
    ) {
        donationService.deleteDonation(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}