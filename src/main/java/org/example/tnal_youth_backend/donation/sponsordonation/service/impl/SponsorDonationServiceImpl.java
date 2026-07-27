package org.example.tnal_youth_backend.donation.sponsordonation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.sponsordonation.dto.request.SponsorDonationRequest;
import org.example.tnal_youth_backend.donation.sponsordonation.dto.response.SponsorDonationResponse;
import org.example.tnal_youth_backend.donation.sponsordonation.entity.SponsorDonation;
import org.example.tnal_youth_backend.donation.sponsordonation.mapper.SponsorDonationMapper;
import org.example.tnal_youth_backend.donation.sponsordonation.repository.SponsorDonationRepository;
import org.example.tnal_youth_backend.donation.sponsordonation.service.SponsorDonationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SponsorDonationServiceImpl
        implements SponsorDonationService {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE =
            new BigDecimal("4000");

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private final SponsorDonationRepository repository;
    private final SponsorDonationMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<SponsorDonationResponse>
    getAllSponsorDonations() {

        return repository
                .findAllByOrderByPaidAtDescIdDesc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorDonationResponse>
    searchSponsorDonations(
            String search
    ) {
        String normalizedSearch =
                trimToNull(search);

        if (normalizedSearch == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor name, phone, or email is required"
            );
        }

        return repository
                .searchBySponsor(
                        normalizedSearch
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorDonationResponse>
    filterByPaymentMethod(
            Short paymentMethodId
    ) {
        requireActiveLookup(
                "payment_methods",
                paymentMethodId,
                "Payment method"
        );

        return repository
                .findAllByPaymentMethodIdOrderByPaidAtDescIdDesc(
                        paymentMethodId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SponsorDonationResponse
    getSponsorDonationById(
            Long id
    ) {
        return mapper.toResponse(
                findSponsorDonation(id)
        );
    }

    @Override
    @Transactional
    public SponsorDonationResponse
    createSponsorDonation(
            SponsorDonationRequest request
    ) {
        validateRequest(request);

        SponsorDonation donation =
                SponsorDonation.builder()
                        .sponsorDonationNo(
                                generateSponsorDonationNumber()
                        )
                        .sponsorId(
                                request.sponsorId()
                        )
                        .branchId(
                                request.branchId()
                        )
                        .amountKhr(
                                normalizeAmount(
                                        request.amountKhr()
                                )
                        )
                        .amountUsd(
                                normalizeAmount(
                                        request.amountUsd()
                                )
                        )
                        .exchangeRateKhrPerUsd(
                                normalizeExchangeRate(
                                        request.exchangeRateKhrPerUsd()
                                )
                        )
                        .paymentMethodId(
                                request.paymentMethodId()
                        )
                        .paidAt(
                                request.paidAt()
                        )
                        .paymentReference(
                                trimToNull(
                                        request.paymentReference()
                                )
                        )
                        .receiptFileId(
                                request.receiptFileId()
                        )
                        .recordedById(
                                getCurrentUserId()
                        )
                        .note(
                                trimToNull(
                                        request.note()
                                )
                        )
                        .build();

        donation.calculateTotalAmountUsd();

        try {
            return mapper.toResponse(
                    repository.saveAndFlush(
                            donation
                    )
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw databaseException(
                    "Sponsor donation could not be created",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public SponsorDonationResponse
    updateSponsorDonation(
            Long id,
            SponsorDonationRequest request
    ) {
        SponsorDonation donation =
                findSponsorDonation(id);

        validateRequest(request);

        donation.setSponsorId(
                request.sponsorId()
        );

        donation.setBranchId(
                request.branchId()
        );

        donation.setAmountKhr(
                normalizeAmount(
                        request.amountKhr()
                )
        );

        donation.setAmountUsd(
                normalizeAmount(
                        request.amountUsd()
                )
        );

        donation.setExchangeRateKhrPerUsd(
                normalizeExchangeRate(
                        request.exchangeRateKhrPerUsd()
                )
        );

        donation.setPaymentMethodId(
                request.paymentMethodId()
        );

        donation.setPaidAt(
                request.paidAt()
        );

        donation.setPaymentReference(
                trimToNull(
                        request.paymentReference()
                )
        );

        donation.setReceiptFileId(
                request.receiptFileId()
        );

        donation.setNote(
                trimToNull(
                        request.note()
                )
        );

        donation.calculateTotalAmountUsd();

        try {
            return mapper.toResponse(
                    repository.saveAndFlush(
                            donation
                    )
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw databaseException(
                    "Sponsor donation could not be updated",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void deleteSponsorDonation(
            Long id
    ) {
        SponsorDonation donation =
                findSponsorDonation(id);

        try {
            repository.delete(
                    donation
            );

            repository.flush();

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    getDatabaseMessage(
                            "Sponsor donation cannot be deleted",
                            exception
                    ),
                    exception
            );
        }
    }

    private void validateRequest(
            SponsorDonationRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor donation request is required"
            );
        }

        requireActiveSponsor(
                request.sponsorId()
        );

        requireExistingRecord(
                "branches",
                request.branchId(),
                "Branch"
        );

        requireActiveLookup(
                "payment_methods",
                request.paymentMethodId(),
                "Payment method"
        );

        if (request.receiptFileId() != null) {
            requireExistingRecord(
                    "files",
                    request.receiptFileId(),
                    "Receipt file"
            );
        }

        validateAmounts(
                request.amountKhr(),
                request.amountUsd()
        );
    }

    private void requireActiveSponsor(
            Long sponsorId
    ) {
        if (sponsorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor ID is required"
            );
        }

        Long count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM sponsors
                        WHERE id = ?
                          AND is_active = TRUE
                        """,
                        Long.class,
                        sponsorId
                );

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Sponsor not found or inactive with ID: "
                            + sponsorId
            );
        }
    }

    private void validateAmounts(
            BigDecimal amountKhr,
            BigDecimal amountUsd
    ) {
        BigDecimal safeKhr =
                normalizeAmount(amountKhr);

        BigDecimal safeUsd =
                normalizeAmount(amountUsd);

        if (safeKhr.compareTo(BigDecimal.ZERO) < 0
                || safeUsd.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor donation amounts cannot be negative"
            );
        }

        if (safeKhr.compareTo(BigDecimal.ZERO) == 0
                && safeUsd.compareTo(BigDecimal.ZERO) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one sponsor donation amount must be greater than zero"
            );
        }
    }

    private SponsorDonation findSponsorDonation(
            Long id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor donation ID is required"
            );
        }

        return repository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Sponsor donation not found with ID: "
                                        + id
                        )
                );
    }

    private String generateSponsorDonationNumber() {
        LocalDate today = LocalDate.now();

        String prefix =
                "SPD-"
                        + today.format(
                        DateTimeFormatter.BASIC_ISO_DATE
                )
                        + "-";

        String latest =
                repository
                        .findLatestNumberByPrefix(
                                prefix
                        )
                        .orElse(null);

        int nextSequence = 1;

        if (latest != null) {
            String sequence =
                    latest.substring(
                            prefix.length()
                    );

            try {
                nextSequence =
                        Integer.parseInt(
                                sequence
                        ) + 1;
            } catch (
                    NumberFormatException ignored
            ) {
                nextSequence = 1;
            }
        }

        return prefix
                + String.format(
                Locale.ROOT,
                "%04d",
                nextSequence
        );
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getPrincipal()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        String username =
                authentication.getName();

        List<Long> userIds =
                jdbcTemplate.query(
                        """
                        SELECT id
                        FROM users
                        WHERE phone = ?
                           OR LOWER(email) = LOWER(?)
                        LIMIT 1
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> resultSet.getLong("id"),
                        username,
                        username
                );

        if (userIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        return userIds.get(0);
    }

    private void requireActiveLookup(
            String tableName,
            Number id,
            String displayName
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    displayName + " ID is required"
            );
        }

        Long count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM %s
                        WHERE id = ?
                          AND is_active = TRUE
                        """.formatted(tableName),
                        Long.class,
                        id
                );

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    displayName
                            + " not found or inactive with ID: "
                            + id
            );
        }
    }

    private void requireExistingRecord(
            String tableName,
            Number id,
            String displayName
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    displayName + " ID is required"
            );
        }

        Long count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM %s
                        WHERE id = ?
                        """.formatted(tableName),
                        Long.class,
                        id
                );

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    displayName
                            + " not found with ID: "
                            + id
            );
        }
    }

    private BigDecimal normalizeAmount(
            BigDecimal amount
    ) {
        return amount == null
                ? ZERO
                : amount;
    }

    private BigDecimal normalizeExchangeRate(
            BigDecimal exchangeRate
    ) {
        if (exchangeRate == null
                || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            return DEFAULT_EXCHANGE_RATE;
        }

        return exchangeRate;
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private ResponseStatusException databaseException(
            String defaultMessage,
            DataIntegrityViolationException exception
    ) {
        String message =
                getDatabaseMessage(
                        defaultMessage,
                        exception
                );

        HttpStatus status =
                message
                        .toLowerCase(Locale.ROOT)
                        .contains("duplicate")
                        ? HttpStatus.CONFLICT
                        : HttpStatus.BAD_REQUEST;

        return new ResponseStatusException(
                status,
                message,
                exception
        );
    }

    private String getDatabaseMessage(
            String defaultMessage,
            DataIntegrityViolationException exception
    ) {
        Throwable cause =
                exception.getMostSpecificCause();

        if (cause == null
                || cause.getMessage() == null
                || cause.getMessage().isBlank()) {
            return defaultMessage;
        }

        return cause.getMessage();
    }
}
