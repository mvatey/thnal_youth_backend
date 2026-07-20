package org.example.tnal_youth_backend.member.branch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchResponse>>
    getAllBranches() {
        return ResponseEntity.ok(
                branchService.getAllBranches()
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
}