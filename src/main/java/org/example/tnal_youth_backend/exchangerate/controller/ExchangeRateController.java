package org.example.tnal_youth_backend.exchangerate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.example.tnal_youth_backend.exchangerate.dto.request.CreateExchangeRateRequest;
import org.example.tnal_youth_backend.exchangerate.dto.response.ExchangeRateResponse;
import org.example.tnal_youth_backend.exchangerate.service.ExchangeRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExchangeRateResponse> createRate(
            @Valid
            @RequestBody CreateExchangeRateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        exchangeRateService.createRate(
                                request,
                                getCurrentUserId(authentication)
                        )
                );
    }

    @GetMapping("/current")
    public ResponseEntity<ExchangeRateResponse> getCurrentRate(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(
                exchangeRateService.getCurrentRate(from, to)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<ExchangeRateResponse>> getHistory(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(
                exchangeRateService.getRateHistory(from, to)
        );
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ExchangeRateResponse> getRateForDate(
            @PathVariable LocalDate date,
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(
                exchangeRateService.getRateForDate(
                        from,
                        to,
                        date
                )
        );
    }

    private Long getCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }
}