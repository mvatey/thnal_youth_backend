package org.example.tnal_youth_backend.activity.income.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ActivityIncomeBatchRequest {

    @NotNull(message = "received_at is required")
    @JsonProperty("received_at")
    private OffsetDateTime receivedAt;

    @Valid
    @NotEmpty(message = "items must contain at least one income row")
    private List<ActivityIncomeItemRequest> items;
}
