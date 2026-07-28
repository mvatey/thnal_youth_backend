package org.example.tnal_youth_backend.member.member.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MemberPageResponse(
        List<MemberListResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}