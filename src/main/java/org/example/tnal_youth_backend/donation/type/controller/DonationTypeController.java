package org.example.tnal_youth_backend.donation.type.controller;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.type.dto.DonationTypeResponse;
import org.example.tnal_youth_backend.donation.type.service.DonationTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donation-types")
@RequiredArgsConstructor
public class DonationTypeController {

    private final DonationTypeService donationTypeService;

    @GetMapping
    public ResponseEntity<List<DonationTypeResponse>>
    getAllDonationTypes(

            @RequestParam(
                    name = "activeOnly",
                    defaultValue = "false"
            )
            Boolean activeOnly
    ) {

        List<DonationTypeResponse> response =
                donationTypeService
                        .getAllDonationTypes(
                                activeOnly
                        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationTypeResponse>
    getDonationTypeById(

            @PathVariable
            Short id
    ) {

        DonationTypeResponse response =
                donationTypeService
                        .getDonationTypeById(
                                id
                        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DonationTypeResponse>
    getDonationTypeByCode(

            @PathVariable
            String code
    ) {

        DonationTypeResponse response =
                donationTypeService
                        .getDonationTypeByCode(
                                code
                        );

        return ResponseEntity.ok(
                response
        );
    }
}