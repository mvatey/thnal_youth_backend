package org.example.tnal_youth_backend.member.branch.service;

import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;

import java.util.List;

public interface BranchService {

    List<BranchResponse> getAllBranches();

    BranchResponse getBranchById(Long id);

    BranchResponse createBranch(
            CreateBranchRequest request
    );

    BranchResponse updateBranch(
            Long id,
            UpdateBranchRequest request
    );

    void deleteBranch(Long id);
}