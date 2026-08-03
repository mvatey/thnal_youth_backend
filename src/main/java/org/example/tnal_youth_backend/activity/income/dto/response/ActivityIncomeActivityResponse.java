package org.example.tnal_youth_backend.activity.income.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeActivityResponse {
    private Long id;
    private String titleKm;
    private String titleEn;
    private Long branchId;
    private String branchNameKm;
    private String branchNameEn;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
}
