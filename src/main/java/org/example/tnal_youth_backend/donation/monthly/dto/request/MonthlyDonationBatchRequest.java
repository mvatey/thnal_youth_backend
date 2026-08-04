package org.example.tnal_youth_backend.donation.monthly.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class MonthlyDonationBatchRequest {

    @NotNull
    @JsonProperty("branch_id")
    private Long branchId;

    /** Use the first day of the selected month, for example 2026-08-01. */
    @NotNull
    @JsonProperty("donation_period")
    private LocalDate donationPeriod;

    @NotNull
    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    @Valid
    @NotEmpty
    private List<MonthlyDonationItemRequest> items;
}
