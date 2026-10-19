package org.example.tnal_youth_backend.member.branch.dto.response;

/**
 * One accessible branch together with where it sits in the province /
 * district / commune hierarchy -- enough for a client to work out "this
 * branch plus everything under it" without another round trip. The same
 * rule as StaffBranchScopeService#computeRegionalCoverage: a province
 * covers every branch sharing its provinceId, a district every branch
 * sharing its districtId, a commune only itself.
 */
public record BranchAreaOptionResponse(
        Long id,
        String branchCode,
        String nameKm,
        String nameEn,
        String levelCode,
        Short provinceId,
        Integer districtId
) {
}
