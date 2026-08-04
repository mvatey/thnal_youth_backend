package org.example.tnal_youth_backend.donation.monthly.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.request.MonthlyDonationBatchRequest;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationBatchResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationDetailResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationMemberPageResponse;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MonthlyDonationPageResponse;
import org.example.tnal_youth_backend.donation.monthly.service.MonthlyDonationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/donations/monthly")
@RequiredArgsConstructor
public class MonthlyDonationController {

    private static final String STAFF =
            "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final MonthlyDonationService monthlyDonationService;

    @GetMapping("/members")
    @PreAuthorize(STAFF)
    public ApiResponse<MonthlyDonationMemberPageResponse> listMembers(
            @RequestParam Long branchId,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                monthlyDonationService.listMembers(
                        branchId,
                        month,
                        year,
                        search,
                        page,
                        size
                )
        );
    }

    @PostMapping("/batch")
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<MonthlyDonationBatchResponse>> createBatch(
            @Valid @RequestBody MonthlyDonationBatchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(monthlyDonationService.createBatch(request)));
    }

    @GetMapping
    @PreAuthorize(STAFF)
    public ApiResponse<MonthlyDonationPageResponse> listMonthlyDonations(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                monthlyDonationService.listMonthlyDonations(
                        branchId,
                        month,
                        year,
                        search,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{branchId}")
    @PreAuthorize(STAFF)
    public ApiResponse<MonthlyDonationDetailResponse> getMonthlyDonationDetail(
            @PathVariable Long branchId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate donationPeriod
    ) {
        return ApiResponse.ok(
                monthlyDonationService.getMonthlyDonationDetail(branchId, donationPeriod)
        );
    }


    @DeleteMapping("/{donationId}")
    @PreAuthorize(STAFF)
    public ResponseEntity<Void> deleteMonthlyDonation(
            @PathVariable Long donationId
    ) {
        monthlyDonationService.deleteMonthlyDonation(donationId);
        return ResponseEntity.noContent().build();
    }

}
