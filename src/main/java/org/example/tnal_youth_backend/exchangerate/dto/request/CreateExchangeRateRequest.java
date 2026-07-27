package org.example.tnal_youth_backend.exchangerate.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CreateExchangeRateRequest {

    @NotBlank(message = "From currency is required")
    @Size(min = 3, max = 3, message = "Currency code must contain 3 characters")
    @JsonProperty("from_currency")
    private String fromCurrency;

    @NotBlank(message = "To currency is required")
    @Size(min = 3, max = 3, message = "Currency code must contain 3 characters")
    @JsonProperty("to_currency")
    private String toCurrency;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(
            value = "0.000001",
            message = "Exchange rate must be greater than zero"
    )
    private BigDecimal rate;

    @NotNull(message = "Effective date is required")
    @JsonProperty("effective_from")
    private LocalDate effectiveFrom;
}