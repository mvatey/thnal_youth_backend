package org.example.tnal_youth_backend.member.branch.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchOptionResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.dto.request.AssignBranchLeaderRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchLeaderResponse;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(
        name = "B. Member Page - Branch"
)
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BranchResponse>>
    getAllBranches() {
        return ResponseEntity.ok(
                branchService.getAllBranches()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
                        branchService.createBranch(request)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void>
    deleteBranch(
            @PathVariable Long id
    ) {
        branchService.deleteBranch(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}/leader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchLeaderResponse> getLeader(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getLeader(id));
    }

    @PutMapping("/{id}/leader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchLeaderResponse> assignLeader(
            @PathVariable Long id, @Valid @RequestBody AssignBranchLeaderRequest request) {
        return ResponseEntity.ok(branchService.assignLeader(id, request.memberId()));
    }

    @DeleteMapping("/{id}/leader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeLeader(@PathVariable Long id) {
        branchService.removeLeader(id);
        return ResponseEntity.noContent().build();
    }
}
