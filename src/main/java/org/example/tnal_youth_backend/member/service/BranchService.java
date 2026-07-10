package org.example.tnal_youth_backend.member.service;

import org.example.tnal_youth_backend.member.dto.BranchDto;
import org.example.tnal_youth_backend.member.entity.Branch;
import org.example.tnal_youth_backend.member.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {
    private final BranchRepository repository;

    public BranchService(BranchRepository repository) {
        this.repository = repository;
    }

    public List<Branch> getAllBranches() {
        return repository.findAll();
    }

    public Branch getBranchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id " + id));
    }

    public Branch createBranch(BranchDto dto) {
        Branch branch = new Branch();
        branch.setBranchCode(dto.getBranchCode());
        branch.setNameEn(dto.getNameEn());
        branch.setNameKh(dto.getNameKh());
        return repository.save(branch);
    }

    public Branch updateBranch(Long id, BranchDto dto) {
        Branch branch = getBranchById(id);
        branch.setBranchCode(dto.getBranchCode());
        branch.setNameEn(dto.getNameEn());
        branch.setNameKh(dto.getNameKh());
        return repository.save(branch);
    }

    public void deleteBranch(Long id) {
        repository.deleteById(id);
    }
}
