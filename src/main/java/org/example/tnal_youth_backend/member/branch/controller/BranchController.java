package org.example.tnal_youth_backend.member.branch.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.branch.dto.request.AssignBranchLeaderRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.*;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(
        name = "2.1 Branch Page - Branch"
)
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<BranchPageResponse>
    getBranchPage(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Short levelId,

            @RequestParam(required = false)
            Short provinceId,

            @RequestParam(required = false)
            Integer districtId,

            @RequestParam(required = false)
            Short statusId
    ) {
        return ResponseEntity.ok(
                branchService.getBranchPage(
                        page,
                        size,
                        search,
                        levelId,
                        provinceId,
                        districtId,
                        statusId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse>
    getBranchById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                branchService.getBranchById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponse>
    createBranch(
            @Valid
            @RequestBody
            CreateBranchRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        branchService.createBranch(
                                request
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse>
    updateBranch(
            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateBranchRequest request
    ) {
        return ResponseEntity.ok(
                branchService.updateBranch(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteBranch(
            @PathVariable Long id
    ) {
        branchService.deleteBranch(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/summary")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<BranchSummaryResponse>
    getBranchSummary() {
        return ResponseEntity.ok(
                branchService
                        .getBranchSummary()
        );
    }

    @GetMapping("/{branchId}/details")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'SECRETARY',
            'BRANCH_LEADER'
        )
        """)
    public ResponseEntity<BranchDetailPageResponse>
    getBranchDetails(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(
                branchService.getBranchDetails(
                        branchId
                )
        );
    }

    @GetMapping("/{branchId}/members")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<BranchMemberPageResponse>
    getBranchMembers(
            @PathVariable Long branchId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(required = false)
            Gender gender,

            @RequestParam(required = false)
            Short statusId
    ) {
        return ResponseEntity.ok(
                branchService.getBranchMembers(
                        branchId,
                        page,
                        size,
                        search,
                        gender,
                        statusId
                )
        );
    }

    @PutMapping("/{branchId}/leader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void>
    assignBranchLeader(
            @PathVariable Long branchId,
            @Valid @RequestBody
            AssignBranchLeaderRequest request
    ) {
        branchService.assignBranchLeader(
                branchId,
                request.memberId()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{branchId}/leader-candidates")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SECRETARY', 'BRANCH_LEADER')"
    )
    public ResponseEntity<List<BranchLeaderCandidateResponse>>
    getBranchLeaderCandidates(
            @PathVariable Long branchId
    ) {
        return ResponseEntity.ok(
                branchService
                        .getBranchLeaderCandidates(
                                branchId
                        )
        );
    }
}