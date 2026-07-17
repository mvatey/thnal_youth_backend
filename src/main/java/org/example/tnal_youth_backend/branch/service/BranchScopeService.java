package org.example.tnal_youth_backend.branch.service;

import java.util.List;

public interface BranchScopeService {

    Long getRootBranchId();

    List<Long> getAccessibleBranchIds();

    boolean canAccessBranch(Long branchId);

    void validateBranchAccess(Long branchId);
}