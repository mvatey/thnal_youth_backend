package org.example.tnal_youth_backend.member.controller;

import org.example.tnal_youth_backend.member.dto.BranchDto;
import org.example.tnal_youth_backend.member.entity.Branch;
import org.example.tnal_youth_backend.member.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService service;

    public BranchController(BranchService service) {
        this.service = service;
    }

    @GetMapping
    public List<Branch> getAllBranches() {
        return service.getAllBranches();
    }

    @GetMapping("/{id}")
    public Branch getBranchById(@PathVariable Long id) {
        return service.getBranchById(id);
    }

    @PostMapping
    public Branch createBranch(@RequestBody BranchDto dto) {
        return service.createBranch(dto);
    }

    @PutMapping("/{id}")
    public Branch updateBranch(@PathVariable Long id, @RequestBody BranchDto dto) {
        return service.updateBranch(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        service.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
