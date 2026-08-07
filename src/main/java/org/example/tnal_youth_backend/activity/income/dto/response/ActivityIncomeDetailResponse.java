package org.example.tnal_youth_backend.activity.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeDetailResponse {
    private ActivityIncomeActivityResponse activity;
    private ActivityIncomeSummaryResponse summary;
    private List<ActivityIncomeMemberRowResponse> items;
}
