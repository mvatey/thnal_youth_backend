package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record MemberListResponse(

        Long id,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        GenderResponse gender,

        BranchResponse branch,

        LookupResponse status,

        LookupResponse level,

        @JsonProperty("profile_photo")
        ProfilePhotoResponse profilePhoto,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        /*
         * Was missing from every list/search/filter query below (only
         * MemberDetailResponse ever carried it) — pages that render this
         * list response, such as the activity participants roster, read
         * member.email directly and had no way to get it unless a
         * separate record (e.g. an activity participant snapshot)
         * happened to carry it too.
         */
        String email,

        /*
         * The member's linked login account role (ADMIN/SECRETARY/
         * BRANCH_LEADER/MEMBER) — this is what pages like the activity
         * participants roster actually mean by "role" (តួនាទី). It was
         * previously read from `level` (member_levels — a separate rank/
         * tier concept, unrelated to account role) purely because both
         * happen to be labelled "role" in the UI, so it looked populated
         * for members whose level label happened to coincide with a role
         * name and showed "-" for everyone else. `null` here means this
         * member has no linked user account at all (users.member_id),
         * which is legitimate — not every member has a login.
         */
        @JsonProperty("account_role")
        AccountRoleResponse accountRole
) {

    public record GenderResponse(

            String code,

            @JsonProperty("label_km")
            String labelKm
    ) {
    }

    public record BranchResponse(

            Long id,

            @JsonProperty("label_km")
            String labelKm
    ) {
    }

    public record LookupResponse(

            Short id,

            String code,

            @JsonProperty("label_km")
            String labelKm,

            @JsonProperty("label_en")
            String labelEn
    ) {
    }

    public record ProfilePhotoResponse(

            Long id,

            String url
    ) {
    }

    public record AccountRoleResponse(

            String code,

            @JsonProperty("label_km")
            String labelKm
    ) {
    }
}