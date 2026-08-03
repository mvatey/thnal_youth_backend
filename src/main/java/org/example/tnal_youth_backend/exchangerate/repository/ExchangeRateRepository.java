package org.example.tnal_youth_backend.exchangerate.repository;

import org.example.tnal_youth_backend.exchangerate.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository
        extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate>
    findByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndActiveTrue(
            String fromCurrency,
            String toCurrency
    );

    List<ExchangeRate>
    findAllByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByEffectiveFromDesc(
            String fromCurrency,
            String toCurrency
    );

    Optional<ExchangeRate>
    findFirstByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
            String fromCurrency,
            String toCurrency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    );

    Optional<ExchangeRate>
    findFirstByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
            String fromCurrency,
            String toCurrency,
            LocalDate date
    );
}