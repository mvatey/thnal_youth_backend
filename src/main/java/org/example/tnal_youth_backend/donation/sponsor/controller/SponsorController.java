package org.example.tnal_youth_backend.donation.sponsor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.sponsor.dto.request.SponsorRequest;
import org.example.tnal_youth_backend.donation.sponsor.dto.response.SponsorResponse;
import org.example.tnal_youth_backend.donation.sponsor.service.SponsorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;

    /*
     * Return all sponsors.
     *
     * No query parameters are required.
     */
    @GetMapping
    public ResponseEntity<List<SponsorResponse>>
    getAllSponsors() {

        return ResponseEntity.ok(
                sponsorService.getAllSponsors()
        );
    }

    /*
     * Used by a dropdown on the Donation form.
     */
    @GetMapping("/active")
    public ResponseEntity<List<SponsorResponse>>
    getActiveSponsors() {

        return ResponseEntity.ok(
                sponsorService.getActiveSponsors()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SponsorResponse>
    getSponsorById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                sponsorService.getSponsorById(
                        id
                )
        );
    }

    @PostMapping
    public ResponseEntity<SponsorResponse>
    createSponsor(
            @Valid
            @RequestBody
            SponsorRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        sponsorService.createSponsor(
                                request
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SponsorResponse>
    updateSponsor(
            @PathVariable Long id,

            @Valid
            @RequestBody
            SponsorRequest request
    ) {
        return ResponseEntity.ok(
                sponsorService.updateSponsor(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteSponsor(
            @PathVariable Long id
    ) {
        sponsorService.deleteSponsor(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}