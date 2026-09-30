package org.example.tnal_youth_backend.member.branch.service;

import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchOptionResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchLeaderResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.*;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.util.List;
import java.util.Set;

public interface BranchService {

    List<BranchOptionResponse> getAccessibleBranchOptions();

    List<BranchOptionResponse> getAllActiveBranchOptions();

    /**
     * Every branch a SECRETARY_REGIONAL position anchored at
     * {@code branchId} would cover -- lets the member/create form show
     * the computed (locked) branch list before actually saving anything.
     * Uses the exact same live computation
     * StaffBranchScopeService#computeRegionalCoverage uses for real
     * authorization, so this preview can never drift out of sync with
     * what the account will actually be granted.
     */
    List<BranchOptionResponse> getRegionalCoverageOptions(Long branchId);

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

    List<BranchLeaderResponse> getLeaders(Long branchId);

    BranchLeaderResponse assignLeader(Long branchId, Long memberId, Short positionId);

    void removeLeader(Long branchId, Long memberId);

    Branch getAccessibleBranchById(
            Long branchId
    );

    BranchSummaryResponse getBranchSummary();

    BranchPageResponse getBranchPage(
            int page,
            int size,
            String search,
            Short levelId,
            Short provinceId,
            Integer districtId,
            Short statusId
    );

    BranchDetailPageResponse getBranchDetails(Long branchId);

    BranchMemberPageResponse getBranchMembers(
            Long branchId,
            int page,
            int size,
            String search,
            Gender gender,
            Short statusId
    );

    void assignBranchLeader(
            Long branchId,
            Long memberId,
            Short positionId
    );

    List<BranchLeaderCandidateResponse>
    getBranchLeaderCandidates(
            Long branchId
    );

    Set<Long> getAccessibleBranchIds();
}
