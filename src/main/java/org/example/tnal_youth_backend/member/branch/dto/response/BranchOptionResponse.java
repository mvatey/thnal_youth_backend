package org.example.tnal_youth_backend.member.branch.dto.response;

public record BranchOptionResponse(
        Long id,
        String branchCode,
        String nameKm,
        String nameEn
) {
}