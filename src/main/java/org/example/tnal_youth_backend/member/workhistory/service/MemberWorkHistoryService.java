package org.example.tnal_youth_backend.member.workhistory.service;

import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;

import java.util.List;

public interface MemberWorkHistoryService {

    List<MemberWorkHistoryResponse> getByMemberId(
            Long memberId
    );

    MemberWorkHistoryResponse create(
            Long memberId,
            MemberWorkHistoryRequest request
    );

    MemberWorkHistoryResponse update(
            Long memberId,
            Long workId,
            MemberWorkHistoryRequest request
    );

    void delete(
            Long memberId,
            Long workId
    );
}