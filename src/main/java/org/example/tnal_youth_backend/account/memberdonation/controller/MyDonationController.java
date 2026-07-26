package org.example.tnal_youth_backend.account.memberdonation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationSummaryResponse;
import org.example.tnal_youth_backend.account.memberdonation.service.MyDonationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-account/donations")
@RequiredArgsConstructor
@Tag(
        name = "A. My Account - Donations",
        description = "ការបរិច្ចាគ (My Account)"
)
public class MyDonationController {

    private final MyDonationService myDonationService;

    /*
     * Get all donations belonging to the logged-in member.
     *
     * GET /api/my-account/donations
     */
    @GetMapping
    public ResponseEntity<List<MyDonationResponse>>
    getMyDonations() {

        return ResponseEntity.ok(
                myDonationService.getMyDonations()
        );
    }

    /*
     * Search the logged-in member's donations
     * by month and year.
     *
     * Required format:
     * yyyy-MM
     *
     * Example:
     * GET /api/my-account/donations/search?period=2026-07
     */
    @GetMapping("/search")
    public ResponseEntity<List<MyDonationResponse>>
    searchByDonationPeriod(

            @RequestParam
            String period
    ) {
        return ResponseEntity.ok(
                myDonationService.searchByDonationPeriod(
                        period
                )
        );
    }

    /*
     * Filter the logged-in member's donations
     * by payment method.
     *
     * Example:
     * GET /api/my-account/donations/filter/payment-method
     *     ?paymentMethodId=1
     */
    @GetMapping("/filter/payment-method")
    public ResponseEntity<List<MyDonationResponse>>
    filterByPaymentMethod(

            @RequestParam
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                myDonationService.filterByPaymentMethod(
                        paymentMethodId
                )
        );
    }

    /*
     * Get summary totals for the logged-in member.
     *
     * GET /api/my-account/donations/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<MyDonationSummaryResponse>
    getMyDonationSummary() {

        return ResponseEntity.ok(
                myDonationService.getMyDonationSummary()
        );
    }

    /*
     * Get one donation only when it belongs
     * to the logged-in member.
     *
     * GET /api/my-account/donations/{donationId}
     */
    @GetMapping("/{donationId}")
    public ResponseEntity<MyDonationResponse>
    getMyDonationById(

            @PathVariable
            Long donationId
    ) {
        return ResponseEntity.ok(
                myDonationService.getMyDonationById(
                        donationId
                )
        );
    }
}