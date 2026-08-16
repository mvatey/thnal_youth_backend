package org.example.tnal_youth_backend.member.personalinfo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.personalinfo.dto.request.AssignMemberBranchRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberBranchAssignmentService;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberPersonalInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * Assigns or removes a secretary's ADDITIONAL (non-primary) branches.
 * Kept as its own controller/service, separate from
 * MemberPersonalInfoController, so this feature doesn't depend on —
 * or risk conflicting with — that controller's other endpoints.
 */
@RestController
@RequestMapping(
        "/api/members/{memberId}/personal-info/branches"
)
@RequiredArgsConstructor
@Tag(
        name = "3.0.2 Member Page - Additional Branch Assignments",
        description = """
                Assign or remove a secretary's additional
                (non-primary) branches
                """
)
public class MemberBranchAssignmentController {

    private final MemberBranchAssignmentService
            memberBranchAssignmentService;

    private final MemberPersonalInfoService
            memberPersonalInfoService;

    @PostMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberPersonalInfoResponse>
    assignBranch(
            @PathVariable
            Long memberId,

            @Valid
            @RequestBody
            AssignMemberBranchRequest request
    ) {
        memberBranchAssignmentService
                .assignBranch(
                        memberId,
                        request.branchId()
                );

        return ResponseEntity.ok(
                memberPersonalInfoService
                        .getPersonalInfo(
                                memberId
                        )
        );
    }

    @DeleteMapping("/{branchId}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'BRANCH_LEADER'
            )
            """)
    public ResponseEntity<MemberPersonalInfoResponse>
    removeBranch(
            @PathVariable
            Long memberId,

            @PathVariable
            Long branchId
    ) {
        memberBranchAssignmentService
                .removeBranch(
                        memberId,
                        branchId
                );

        return ResponseEntity.ok(
                memberPersonalInfoService
                        .getPersonalInfo(
                                memberId
                        )
        );
    }
}
