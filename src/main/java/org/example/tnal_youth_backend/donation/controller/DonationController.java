package org.example.tnal_youth_backend.donation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.response.ApiResponse;
import org.example.tnal_youth_backend.donation.dto.DonationCreateDTO;
import org.example.tnal_youth_backend.donation.dto.DonationCreateResultDTO;
import org.example.tnal_youth_backend.donation.dto.DonationDTO;
import org.example.tnal_youth_backend.donation.dto.DonationPageDTO;
import org.example.tnal_youth_backend.donation.dto.DonationSummaryDTO;
import org.example.tnal_youth_backend.donation.dto.DonationUpdateDTO;
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
 * recording/correcting/reporting is for organisational staff
 * (ADMIN / SECRETARY / BRANCH_LEADER); plain MEMBERs cannot touch financial
 * records. DELETE is ADMIN-only — a booked donation is not casually removable.
 */
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private static final String STAFF = "hasAnyRole('ADMIN','SECRETARY','BRANCH_LEADER')";

    private final DonationService service;

    @PostMapping
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationCreateResultDTO>> create(
            @Valid @RequestBody DonationCreateDTO req) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(req)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }

    @GetMapping
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationPageDTO>> list(
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
    public ResponseEntity<ApiResponse<DonationSummaryDTO>> summary(
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
    @PreAuthorize(STAFF)
    public ResponseEntity<ApiResponse<DonationDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody DonationUpdateDTO req) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(Boolean.TRUE));
    }
}
