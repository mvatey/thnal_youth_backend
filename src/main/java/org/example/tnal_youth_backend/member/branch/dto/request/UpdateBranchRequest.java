package org.example.tnal_youth_backend.member.branch.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBranchRequest(

        @JsonProperty("name_km")
        @NotBlank(message = "Khmer branch name is required")
        @Size(
                max = 255,
                message = "Khmer branch name must not exceed 255 characters"
        )
        String nameKm,

        @JsonProperty("name_en")
        @Size(
                max = 255,
                message = "English branch name must not exceed 255 characters"
        )
        String nameEn,

        @JsonProperty("branch_level_id")
        @NotNull(message = "Branch level ID is required")
        Short branchLevelId,

        @JsonProperty("parent_branch_id")
        Long parentBranchId,

        // Required for every level except COUNTRY; the service checks it
        // against the chosen level.
        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("district_id")
        Integer districtId,

        @JsonProperty("commune_id")
        Integer communeId,

        @JsonProperty("status_id")
        @NotNull(message = "Branch status ID is required")
        Short statusId,

        @JsonProperty("address")
        String address,

        @JsonProperty("google_map_url")
        String googleMapUrl,

        /** Yes / no; null means not answered. */
        @JsonProperty("has_location")
        Boolean hasLocation,

        /** Yes / no; null means not answered. */
        @JsonProperty("legally_registered")
        Boolean legallyRegistered,

        /** Optional registration document (an uploaded file's id); dropped when not registered by law. */
        @JsonProperty("legal_document_file_id")
        Long legalDocumentFileId,

        @JsonProperty("phone")
        @Size(
                max = 30,
                message = "Phone must not exceed 30 characters"
        )
        String phone,

        @JsonProperty("email")
        @Email(message = "Email format is invalid")
        String email
) {
}