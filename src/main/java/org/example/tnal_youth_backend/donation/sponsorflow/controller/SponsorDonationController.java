package org.example.tnal_youth_backend.donation.sponsorflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.request.SponsorDonationUpsertRequest;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationPageResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationRowResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorDonationSummaryResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.dto.response.SponsorLookupResponse;
import org.example.tnal_youth_backend.donation.sponsorflow.service.SponsorDonationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/donations/sponsor")
@RequiredArgsConstructor
public class SponsorDonationController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";
    private static final String DONATION_ENTRY =
            "hasAnyRole('SECRETARY','BRANCH_LEADER')";

    private final SponsorDonationService service;

    @PostMapping
    @PreAuthorize(DONATION_ENTRY)
    public ResponseEntity<ApiResponse<SponsorDonationRowResponse>> create(
            @Valid @RequestBody SponsorDonationUpsertRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(request)));
    }

    /** Matches the UI filters: search, donor type, branch, and one paid date. */
    @GetMapping
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationPageResponse> list(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String donorKind,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ApiResponse.ok(service.list(
                branchId, donorKind, paidDate, search, page, size
        ));
    }

    @GetMapping("/summary")
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationSummaryResponse> summary(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate
    ) {
        return ApiResponse.ok(service.summary(branchId, paidDate));
    }

    @GetMapping("/lookup/sponsors")
    @PreAuthorize(STAFF)
    public ApiResponse<List<SponsorLookupResponse>> sponsors(
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(service.sponsors(search));
    }

    /** MEMBER flow: choose branch first, then load active members in that branch. */
    @GetMapping("/lookup/members")
    @PreAuthorize(STAFF)
    public ApiResponse<List<SponsorLookupResponse>> members(
            @RequestParam Long branchId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.ok(service.members(branchId, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF)
    public ApiResponse<SponsorDonationRowResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize(DONATION_ENTRY)
    public ApiResponse<SponsorDonationRowResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SponsorDonationUpsertRequest request
    ) {
        return ApiResponse.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARY')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
