package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record BranchTableItemResponse(

        Long id,

        @JsonProperty("branch_code")
        String branchCode,

        @JsonProperty("name_km")
        String nameKm,

        @JsonProperty("name_en")
        String nameEn,

        @JsonProperty("branch_level_id")
        Short branchLevelId,

        @JsonProperty("branch_level_name_km")
        String branchLevelNameKm,

        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("province_name_km")
        String provinceNameKm,

        @JsonProperty("district_id")
        Integer districtId,

        @JsonProperty("district_name_km")
        String districtNameKm,

        @JsonProperty("commune_id")
        Integer communeId,

        @JsonProperty("commune_name_km")
        String communeNameKm,

        @JsonProperty("member_count")
        long memberCount,

        @JsonProperty("status_id")
        Short statusId,

        @JsonProperty("status_name_km")
        String statusNameKm,

        @JsonProperty("created_at")
        OffsetDateTime createdAt
) {
}