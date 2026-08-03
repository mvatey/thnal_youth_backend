package org.example.tnal_youth_backend.exchangerate.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponse {

    private Long id;

    @JsonProperty("from_currency")
    private String fromCurrency;

    @JsonProperty("to_currency")
    private String toCurrency;

    private BigDecimal rate;

    @JsonProperty("effective_from")
    private LocalDate effectiveFrom;

    @JsonProperty("effective_to")
    private LocalDate effectiveTo;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("created_by")
    private Long createdBy;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}