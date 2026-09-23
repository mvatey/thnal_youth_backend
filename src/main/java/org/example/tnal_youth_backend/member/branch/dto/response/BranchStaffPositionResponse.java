package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Shared by BranchLeaderResponse and BranchMemberTableItemResponse --
 * a branch_staff member/leader's own job title within the branch,
 * distinct from their account role, same distinction
 * MemberListResponse.position already draws for the main member list.
 */
public record BranchStaffPositionResponse(

        Short id,

        String code,

        @JsonProperty("label_km")
        String labelKm,

        @JsonProperty("label_en")
        String labelEn
) {
}
