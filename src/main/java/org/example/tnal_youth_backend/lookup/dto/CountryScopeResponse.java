package org.example.tnal_youth_backend.lookup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Whether the signed-in account is staff of the single country-level
 * branch ("semi-admin"), and which branch that is. The sidebar uses it to
 * offer an "all branches" view next to the account's own branch.
 */
public record CountryScopeResponse(
        @JsonProperty("country_staff") boolean countryStaff,
        @JsonProperty("branch_id") Long branchId,
        @JsonProperty("name_km") String nameKm,
        @JsonProperty("name_en") String nameEn
) {
}
