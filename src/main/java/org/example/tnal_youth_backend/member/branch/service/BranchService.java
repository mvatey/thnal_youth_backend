package org.example.tnal_youth_backend.member.branch.service;

import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchOptionResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchLeaderResponse;

import java.util.List;

public interface BranchService {

    List<BranchOptionResponse> getAccessibleBranchOptions();

    List<BranchOptionResponse> getAllActiveBranchOptions();

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

    BranchLeaderResponse getLeader(Long branchId);

    BranchLeaderResponse assignLeader(Long branchId, Long memberId);

    void removeLeader(Long branchId);
}
