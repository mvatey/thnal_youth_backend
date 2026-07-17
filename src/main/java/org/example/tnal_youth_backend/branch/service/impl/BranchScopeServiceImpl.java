package org.example.tnal_youth_backend.branch.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.branch.service.BranchScopeService;
import org.example.tnal_youth_backend.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchScopeServiceImpl implements BranchScopeService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    @Override
    public Long getRootBranchId() {
        User user = currentUserProvider.getCurrentUser();

        return userRepository.findBranchIdByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user is not linked to a member and branch"
                        )
                );
    }

    @Override
    public List<Long> getAccessibleBranchIds() {
        Long rootBranchId = getRootBranchId();

        List<Long> branchIds =
                branchRepository.findBranchAndDescendantIds(
                        rootBranchId
                );

        if (branchIds.isEmpty()) {
            throw new IllegalStateException(
                    "No accessible branches were found"
            );
        }

        return branchIds;
    }

    @Override
    public boolean canAccessBranch(Long branchId) {
        if (branchId == null) {
            return false;
        }

        return getAccessibleBranchIds().contains(branchId);
    }

    @Override
    public void validateBranchAccess(Long branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException(
                    "Branch ID is required"
            );
        }

        if (!canAccessBranch(branchId)) {
            throw new IllegalStateException(
                    "You do not have access to branch " + branchId
            );
        }
    }
}