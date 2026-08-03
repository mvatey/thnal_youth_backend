package org.example.tnal_youth_backend.activity.income.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityIncomeSavedItemResponse {

    @JsonProperty("donation_id")
    private Long donationId;

    @JsonProperty("donation_no")
    private String donationNo;

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("total_amount_usd")
    private BigDecimal totalAmountUsd;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
