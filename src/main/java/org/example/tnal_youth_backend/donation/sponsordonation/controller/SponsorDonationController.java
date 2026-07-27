//package org.example.tnal_youth_backend.donation.sponsordonation.controller;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.example.tnal_youth_backend.donation.sponsordonation.dto.request.SponsorDonationRequest;
//import org.example.tnal_youth_backend.donation.sponsordonation.dto.response.SponsorDonationResponse;
//import org.example.tnal_youth_backend.donation.sponsordonation.service.SponsorDonationService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/sponsor-donations")
//@RequiredArgsConstructor
//@Tag(
//        name = "B. Member Page - Sponsor Donations",
//        description = "Manage sponsorship payments from registered sponsors"
//)
//public class SponsorDonationController {
//
//    private final SponsorDonationService service;
//
//    @GetMapping
//    public ResponseEntity<List<SponsorDonationResponse>>
//    getAllSponsorDonations() {
//        return ResponseEntity.ok(
//                service.getAllSponsorDonations()
//        );
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<List<SponsorDonationResponse>>
//    searchSponsorDonations(
//            @RequestParam
//            String search
//    ) {
//        return ResponseEntity.ok(
//                service.searchSponsorDonations(
//                        search
//                )
//        );
//    }
//
//    @GetMapping("/filter/payment-method")
//    public ResponseEntity<List<SponsorDonationResponse>>
//    filterByPaymentMethod(
//            @RequestParam
//            Short paymentMethodId
//    ) {
//        return ResponseEntity.ok(
//                service.filterByPaymentMethod(
//                        paymentMethodId
//                )
//        );
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<SponsorDonationResponse>
//    getSponsorDonationById(
//            @PathVariable
//            Long id
//    ) {
//        return ResponseEntity.ok(
//                service.getSponsorDonationById(
//                        id
//                )
//        );
//    }
//
//    @PostMapping
//    public ResponseEntity<SponsorDonationResponse>
//    createSponsorDonation(
//            @Valid
//            @RequestBody
//            SponsorDonationRequest request
//    ) {
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(
//                        service.createSponsorDonation(
//                                request
//                        )
//                );
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<SponsorDonationResponse>
//    updateSponsorDonation(
//            @PathVariable
//            Long id,
//
//            @Valid
//            @RequestBody
//            SponsorDonationRequest request
//    ) {
//        return ResponseEntity.ok(
//                service.updateSponsorDonation(
//                        id,
//                        request
//                )
//        );
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void>
//    deleteSponsorDonation(
//            @PathVariable
//            Long id
//    ) {
//        service.deleteSponsorDonation(
//                id
//        );
//
//        return ResponseEntity
//                .noContent()
//                .build();
//    }
//}
