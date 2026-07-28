package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record BranchResponse(

        Long id,

        @JsonProperty("name_km")
        String nameKm,

        @JsonProperty("name_en")
        String nameEn,

        @JsonProperty("branch_level_id")
        Short branchLevelId,

        @JsonProperty("parent_branch_id")
        Long parentBranchId,

        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("district_id")
        Integer districtId,

        @JsonProperty("commune_id")
        Integer communeId,

        @JsonProperty("status_id")
        Short statusId,

        String address,

        @JsonProperty("google_map_url")
        String googleMapUrl,

        String phone,

        String email,

        @JsonProperty("created_by")
        Long createdById,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}