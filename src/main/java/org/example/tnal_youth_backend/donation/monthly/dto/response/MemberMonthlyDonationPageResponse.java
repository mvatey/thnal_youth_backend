package org.example.tnal_youth_backend.donation.monthly.dto.response;

import java.util.List;

public record MemberMonthlyDonationPageResponse(

        List<MemberMonthlyDonationResponse> content,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last
) {
}