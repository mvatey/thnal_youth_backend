package org.example.tnal_youth_backend.account.memberworkhistory.service;

import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;

import java.util.List;

public interface MyWorkHistoryService {

    /*
     * Get every work-history record belonging to
     * the currently logged-in member.
     */
    List<MemberWorkHistoryResponse> getMyWorkHistory();

    /*
     * Get one work-history record belonging to
     * the currently logged-in member.
     */
    MemberWorkHistoryResponse getMyWorkHistoryById(
            Long workId
    );

    /*
     * Create a work-history record for
     * the currently logged-in member.
     */
    MemberWorkHistoryResponse createMyWorkHistory(
            MemberWorkHistoryRequest request
    );

    /*
     * Update a work-history record belonging to
     * the currently logged-in member.
     */
    MemberWorkHistoryResponse updateMyWorkHistory(
            Long workId,
            MemberWorkHistoryRequest request
    );

    /*
     * Delete a work-history record belonging to
     * the currently logged-in member.
     */
    void deleteMyWorkHistory(
            Long workId
    );
}