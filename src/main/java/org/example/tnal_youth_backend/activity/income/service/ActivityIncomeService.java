package org.example.tnal_youth_backend.activity.income.service;

import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.MemberActivityIncomeHistoryResponse;

import java.time.OffsetDateTime;

public interface ActivityIncomeService {

    MemberActivityIncomeHistoryResponse getMemberHistory(
            Long memberId,
            int page,
            int size
    );

    ActivityIncomeBatchResponse createBatch(
            Long activityId,
            ActivityIncomeBatchRequest request
    );

    ActivityIncomePageResponse list(
            Long branchId,
            String search,
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo,
            int page,
            int size
    );

    ActivityIncomeDetailResponse getDetail(Long activityId);

    void deleteIncome(Long activityId, Long donationId);
}
