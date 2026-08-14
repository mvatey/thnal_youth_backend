package org.example.tnal_youth_backend.donation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.example.tnal_youth_backend.donation.dto.request.DonationCreateRequest;
import org.example.tnal_youth_backend.donation.dto.response.DonationCreateResultResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationPageResponse;
import org.example.tnal_youth_backend.donation.dto.response.DonationSummaryResponse;
import org.example.tnal_youth_backend.donation.dto.request.DonationUpdateRequest;
import org.example.tnal_youth_backend.donation.service.DonationService;
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

/**
 * Donation recording + reporting endpoints.
 *
 * <p>Authorization (method-level, same approach as NotificationController):
 * reporting (list/get/summary) is for organisational staff
 * (ADMIN / SECRETARY / BRANCH_LEADER); plain MEMBERs cannot touch financial
 * records here (see the self-service {@code account.memberdonation} module
 * for a member's own donations). Recording/correcting a donation
 * (create/update) is entry-staff-only (SECRETARY / BRANCH_LEADER) — ADMIN is
 * intentionally excluded, matching the frontend's admin-is-view-only rule.
 * DELETE is ADMIN-only — a booked donation is not casually removable.
 */
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private static final String STAFF = "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";
    private static final String DONATION_ENTRY = "hasAnyRole('SECRETARY','BRANCH_LEADER')";

    private final DonationService service;

    @PostMapping
    @PreAuthorize(DONATION_ENTRY)
    public ResponseEntity<ApiResponse<DonationCreateResultResponse>> create(
            @Valid @RequestBody DonationCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(req)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }

    @GetMapping
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationPageResponse>> list(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Short typeId,
            @RequestParam(required = false) Short paymentMethodId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long sponsorId,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) OffsetDateTime paidFrom,
            @RequestParam(required = false) OffsetDateTime paidTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(service.list(
                branchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, search, page, size)));
    }

    @GetMapping("/summary")
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationSummaryResponse>> summary(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Short typeId,
            @RequestParam(required = false) Short paymentMethodId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long sponsorId,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) OffsetDateTime paidFrom,
            @RequestParam(required = false) OffsetDateTime paidTo,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(service.summary(
                branchId, typeId, paymentMethodId, memberId, sponsorId, activityId,
                paidFrom, paidTo, search)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(DONATION_ENTRY)
    public ResponseEntity<ApiResponse<DonationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DonationUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(Boolean.TRUE));
    }
}
