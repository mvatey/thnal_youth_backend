package org.example.tnal_youth_backend.donation.monthly.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.monthly.dto.response.MemberMonthlyDonationPageResponse;
import org.example.tnal_youth_backend.donation.monthly.service.MonthlyDonationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/members/{memberId}/monthly-donations"
)
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Monthly Donations",
        description = "Read monthly-donation history for a selected member"
)
public class MemberMonthlyDonationController {

    private final MonthlyDonationService
            monthlyDonationService;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'SECRETARY',
                'BRANCH_LEADER',
                'MEMBER'
            )
            """)
    public ResponseEntity<MemberMonthlyDonationPageResponse>
    getMemberMonthlyDonations(
            @PathVariable
            Long memberId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Short paymentMethodId
    ) {
        return ResponseEntity.ok(
                monthlyDonationService
                        .listMemberMonthlyDonations(
                                memberId,
                                search,
                                paymentMethodId,
                                page,
                                size
                        )
        );
    }
}