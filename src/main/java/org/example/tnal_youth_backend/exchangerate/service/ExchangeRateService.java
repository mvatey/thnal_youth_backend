package org.example.tnal_youth_backend.exchangerate.service;

import org.example.tnal_youth_backend.exchangerate.dto.request.CreateExchangeRateRequest;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;

import java.time.LocalDate;
import java.util.List;

public interface ExchangeRateService {

    ExchangeRateResponse createRate(
            CreateExchangeRateRequest request,
            Long currentUserId
    );

    ExchangeRateResponse getCurrentRate(
            String fromCurrency,
            String toCurrency
    );

    ExchangeRateResponse getRateForDate(
            String fromCurrency,
            String toCurrency,
            LocalDate date
    );

    List<ExchangeRateResponse> getRateHistory(
            String fromCurrency,
            String toCurrency
    );
}