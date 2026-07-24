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
        description = " ការបរិច្ចាគ (my - account ) "
)
public class MyDonationController {

    private final MyDonationService myDonationService;

    /*
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
     * GET /api/my-account/donations/{donationId}
     */
    @GetMapping("/{donationId}")
    public ResponseEntity<MyDonationResponse>
    getMyDonationById(
            @PathVariable Long donationId
    ) {
        return ResponseEntity.ok(
                myDonationService.getMyDonationById(
                        donationId
                )
        );
    }
}