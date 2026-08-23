package org.example.tnal_youth_backend.member.personalinfo.service;

import java.util.List;

/*
 * Manages a SECRETARY's ADDITIONAL branch coverage (branch_staff),
 * separate from the member's primary branch (members.branch_id,
 * still changed via the existing personal-info branch field/save
 * flow). A member's very first branch, if none is set yet, is
 * promoted to primary automatically by assignBranch — see the impl.
 */
public interface MemberBranchAssignmentService {

    void assignBranch(
            Long memberId,
            Long branchId
    );

    void removeBranch(
            Long memberId,
            Long branchId
    );

    /**
     * Atomically replaces the complete branch coverage of a secretary.
     * The primary branch is kept when it remains selected; otherwise the
     * first selected branch becomes primary. A secretary must always have
     * at least one assigned branch.
     */
    void replaceBranches(
            Long memberId,
            List<Long> branchIds
    );
}
