package org.example.tnal_youth_backend.member.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.ViewerScope;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;
import java.util.List;

public record CreateMemberRequest(
        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        Gender gender,

        @JsonProperty("nationality_id")
        Short nationalityId,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        String username,
        String phone,
        String email,

        @JsonProperty("branch_id")
        Long branchId,

        /**
         * Optional. Only meaningful when the resolved role is SECRETARY —
         * a secretary can cover more than one branch. When provided (2+
         * entries), the first branch becomes the member's primary
         * branch (same as branchId) and every branch in the list gets a
         * branch_staff row. Ignored for every other role, which stays
         * single-branch via branchId.
         */
        @JsonProperty("branch_ids")
        List<Long> branchIds,

        @JsonProperty("level_id")
        Short levelId,

        UserRole role,

        @JsonProperty("position_id")
        Short positionId,

        /**
         * Only meaningful when the resolved role is VIEWER -- which
         * branch-scoped level (BRANCH_LEADER or SECRETARY) this new
         * member-linked viewer sees their own branch at. In practice
         * always derived from the chosen position's own
         * mappedViewerScope (see resolveRequestedViewerScope) rather
         * than sent directly, the same way role is normally derived
         * from the position's mappedRole -- accepted here mainly so a
         * request that resolves role without a position (the legacy
         * "role" field above) can still work.
         */
        @JsonProperty("viewer_scope")
        ViewerScope viewerScope,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        @JsonProperty("status_id")
        Short statusId,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId
) {
}
