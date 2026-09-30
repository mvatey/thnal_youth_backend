package org.example.tnal_youth_backend.member.branch.dto.response;

public record BranchOptionResponse(
        Long id,
        String branchCode,
        String nameKm,
        String nameEn,

        /*
         * Cross-reference against /lookups/branch-levels' own "id" to
         * tell a province/district/commune branch apart -- e.g. the
         * member/create branch picker filters this down to province +
         * district only once a SECRETARY_REGIONAL position is chosen.
         */
        Short branchLevelId
) {
}