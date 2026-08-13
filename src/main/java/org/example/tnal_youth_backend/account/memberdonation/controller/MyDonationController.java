package org.example.tnal_youth_backend.account.memberdonation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;
import org.example.tnal_youth_backend.account.memberdonation.service.MyDonationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/donations")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Donations",
        description = "View the logged-in member's monthly and sponsor donations"
)
@PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
public class MyDonationController {

    private final MyDonationService myDonationService;

    /*
     * ==========================================================
     * MONTHLY DONATIONS
     * ==========================================================
     */

    /*
     * GET /api/my-account/donations/monthly
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<MyDonationResponse>>
    getMyMonthlyDonations() {

        return ResponseEntity.ok(
                myDonationService.getMyMonthlyDonations()
        );
    }

    /*
     * GET /api/my-account/donations/monthly/search
     *     ?period=2026-07
     */
    @GetMapping("/monthly/search")
    public ResponseEntity<List<MyDonationResponse>>
    searchMyMonthlyDonations(
            @RequestParam
            String period
    ) {
        return ResponseEntity.ok(
                myDonationService.searchMyMonthlyDonations(
                        period
                )
        );
    }

    /*
     * GET /api/my-account/donations/monthly/filter/payment-method
     *     ?paymentMethodId=1
     */
    @GetMapping("/monthly/filter/payment-method")
    public ResponseEntity<List<MyDonationResponse>>
    filterMyMonthlyDonationsByPaymentMethod(
            @RequestParam
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                myDonationService
                        .filterMyMonthlyDonationsByPaymentMethod(
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
     * GET /api/my-account/donations/sponsors
     */
    @GetMapping("/sponsors")
    public ResponseEntity<List<MyDonationResponse>>
    getMySponsorDonations() {

        return ResponseEntity.ok(
                myDonationService.getMySponsorDonations()
        );
    }

    /*
     * GET /api/my-account/donations/sponsors/search
     *     ?search=ABA
     */
    @GetMapping("/sponsors/search")
    public ResponseEntity<List<MyDonationResponse>>
    searchMySponsorDonations(
            @RequestParam
            String search
    ) {
        return ResponseEntity.ok(
                myDonationService.searchMySponsorDonations(
                        search
                )
        );
    }

    /*
     * GET /api/my-account/donations/sponsors/filter/payment-method
     *     ?paymentMethodId=1
     */
    @GetMapping("/sponsors/filter/payment-method")
    public ResponseEntity<List<MyDonationResponse>>
    filterMySponsorDonationsByPaymentMethod(
            @RequestParam
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                myDonationService
                        .filterMySponsorDonationsByPaymentMethod(
                                paymentMethodId
                        )
        );
    }
}
