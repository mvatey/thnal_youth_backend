package org.example.tnal_youth_backend.exchangerate.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.exchangerate.dto.request.CreateExchangeRateRequest;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;
import org.example.tnal_youth_backend.exchangerate.entity.ExchangeRate;
import org.example.tnal_youth_backend.exchangerate.repository.ExchangeRateRepository;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl
        implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ExchangeRateResponse createRate(
            CreateExchangeRateRequest request,
            Long currentUserId
    ) {
        String fromCurrency =
                normalizeCurrency(request.getFromCurrency());

        String toCurrency =
                normalizeCurrency(request.getToCurrency());

        if (fromCurrency.equals(toCurrency)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source and target currencies must be different"
            );
        }

        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );

        ExchangeRate currentRate =
                exchangeRateRepository
                        .findByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndActiveTrue(
                                fromCurrency,
                                toCurrency
                        )
                        .orElse(null);

        if (currentRate != null) {
            LocalDate newEffectiveDate =
                    request.getEffectiveFrom();

            if (!newEffectiveDate.isAfter(
                    currentRate.getEffectiveFrom()
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "The new effective date must be after "
                                + "the current rate's effective date"
                );
            }

            currentRate.setActive(false);
            currentRate.setEffectiveTo(
                    newEffectiveDate.minusDays(1)
            );

            exchangeRateRepository.saveAndFlush(
                    currentRate
            );
        }

        ExchangeRate newRate = ExchangeRate.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(request.getRate())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(null)
                .active(true)
                .createdBy(createdBy)
                .build();

        ExchangeRate savedRate =
                exchangeRateRepository.saveAndFlush(
                        newRate
                );

        return toResponse(savedRate);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getCurrentRate(
            String fromCurrency,
            String toCurrency
    ) {
        ExchangeRate rate =
                exchangeRateRepository
                        .findByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndActiveTrue(
                                normalizeCurrency(fromCurrency),
                                normalizeCurrency(toCurrency)
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Current exchange rate was not found"
                                )
                        );

        return toResponse(rate);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getRateForDate(
            String fromCurrency,
            String toCurrency,
            LocalDate date
    ) {
        String normalizedFrom =
                normalizeCurrency(fromCurrency);

        String normalizedTo =
                normalizeCurrency(toCurrency);

        ExchangeRate rate =
                exchangeRateRepository
                        .findFirstByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
                                normalizedFrom,
                                normalizedTo,
                                date,
                                date
                        )
                        .or(() ->
                                exchangeRateRepository
                                        .findFirstByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
                                                normalizedFrom,
                                                normalizedTo,
                                                date
                                        )
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No exchange rate exists for date: "
                                                + date
                                )
                        );

        return toResponse(rate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> getRateHistory(
            String fromCurrency,
            String toCurrency
    ) {
        return exchangeRateRepository
                .findAllByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByEffectiveFromDesc(
                        normalizeCurrency(fromCurrency),
                        normalizeCurrency(toCurrency)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Currency is required"
            );
        }

        String normalized =
                currency.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() != 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Currency code must contain 3 characters"
            );
        }

        return normalized;
    }

    private ExchangeRateResponse toResponse(
            ExchangeRate exchangeRate
    ) {
        return ExchangeRateResponse.builder()
                .id(exchangeRate.getId())
                .fromCurrency(exchangeRate.getFromCurrency())
                .toCurrency(exchangeRate.getToCurrency())
                .rate(exchangeRate.getRate())
                .effectiveFrom(exchangeRate.getEffectiveFrom())
                .effectiveTo(exchangeRate.getEffectiveTo())
                .active(exchangeRate.getActive())
                .createdBy(
                        exchangeRate.getCreatedBy() != null
                                ? exchangeRate.getCreatedBy().getId()
                                : null
                )
                .createdAt(exchangeRate.getCreatedAt())
                .updatedAt(exchangeRate.getUpdatedAt())
                .build();
    }
}