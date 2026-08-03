package org.example.tnal_youth_backend.donation.sponsorflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.request.SponsorDonationUpsertRequest;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationPageResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationRowResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorLookupResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.service.SponsorDonationUiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/donations/sponsor")
@RequiredArgsConstructor
public class SponsorDonationController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final SponsorDonationUiService service;

    @PostMapping
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<SponsorDonationRowResponse>> create(
            @Valid
            @RequestBody
            SponsorDonationUpsertRequest request
    ) {
        SponsorDonationRowResponse response =
                service.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.ok(response)
                );
    }

    @GetMapping
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationPageResponse> list(
            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            String donorKind,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            OffsetDateTime paidFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            OffsetDateTime paidTo,

            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "12")
            int size
    ) {
        return ApiResponse.ok(
                service.list(
                        branchId,
                        donorKind,
                        paidFrom,
                        paidTo,
                        search,
                        page,
                        size
                )
        );
    }

    @GetMapping("/summary")
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationSummaryResponse> summary(
            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            OffsetDateTime paidFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            OffsetDateTime paidTo
    ) {
        return ApiResponse.ok(
                service.summary(
                        branchId,
                        paidFrom,
                        paidTo
                )
        );
    }

    @GetMapping("/lookup/sponsors")
    @PreAuthorize(STAFF)
    public ApiResponse<List<SponsorLookupResponse>> sponsors(
            @RequestParam(required = false)
            String search
    ) {
        return ApiResponse.ok(
                service.sponsors(search)
        );
    }

    @GetMapping("/lookup/members")
    @PreAuthorize(STAFF)
    public ApiResponse<List<SponsorLookupResponse>> members(
            @RequestParam(required = false)
            String search
    ) {
        return ApiResponse.ok(
                service.members(search)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationRowResponse> get(
            @PathVariable
            Long id
    ) {
        return ApiResponse.ok(
                service.get(id)
        );
    }

    /**
     * Full form update.
     *
     * The frontend should send all required fields.
     */
    @PutMapping("/{id}")
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationRowResponse> update(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            SponsorDonationUpsertRequest request
    ) {
        return ApiResponse.ok(
                service.update(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARY')"
    )
    public ResponseEntity<Void> delete(
            @PathVariable
            Long id
    ) {
        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}