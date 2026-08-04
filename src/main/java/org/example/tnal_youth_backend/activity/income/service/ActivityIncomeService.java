package org.example.tnal_youth_backend.activity.income.service;

import org.example.tnal_youth_backend.activity.income.dto.request.ActivityIncomeBatchRequest;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeBatchResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomeDetailResponse;
import org.example.tnal_youth_backend.activity.income.dto.response.ActivityIncomePageResponse;

import java.time.OffsetDateTime;

public interface ActivityIncomeService {

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
