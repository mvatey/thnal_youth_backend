package org.example.tnal_youth_backend.member.participation.dto.response;

import java.util.List;

public record MemberParticipationPageResponse(
        List<MemberParticipationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}