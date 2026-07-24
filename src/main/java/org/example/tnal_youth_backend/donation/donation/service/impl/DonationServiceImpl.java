package org.example.tnal_youth_backend.donation.donation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.donation.dto.request.DonationRequest;
import org.example.tnal_youth_backend.donation.donation.dto.response.DonationResponse;
import org.example.tnal_youth_backend.donation.donation.entity.Donation;
import org.example.tnal_youth_backend.donation.donation.mapper.DonationMapper;
import org.example.tnal_youth_backend.donation.donation.repository.DonationRepository;
import org.example.tnal_youth_backend.donation.donation.service.DonationService;
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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl
        implements DonationService {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE =
            new BigDecimal("4000");

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private static final String MONTHLY_DONATION =
            "MONTHLY_DONATION";

    private static final String ACTIVITY_DONATION =
            "ACTIVITY_DONATION";

    private static final String SPONSOR_DONATION =
            "SPONSOR_DONATION";

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponse> getAllDonations() {

        return donationRepository
                .findAllByOrderByPaidAtDesc()
                .stream()
                .map(donationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DonationResponse getDonationById(
            Long id
    ) {
        return donationMapper.toResponse(
                findDonation(id)
        );
    }

    @Override
    @Transactional
    public DonationResponse createDonation(
            DonationRequest request
    ) {
        validateRequest(request);

        Donation donation =
                Donation.builder()
                        .donationNo(
                                generateDonationNumber()
                        )
                        .donationTypeId(
                                request.donationTypeId()
                        )
                        .memberId(
                                request.memberId()
                        )
                        .sponsorId(
                                request.sponsorId()
                        )
                        .donorName(
                                trimToNull(
                                        request.donorName()
                                )
                        )
                        .activityId(
                                request.activityId()
                        )
                        .branchId(
                                request.branchId()
                        )
                        .donationPeriod(
                                request.donationPeriod()
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
            Donation saved =
                    donationRepository.saveAndFlush(
                            donation
                    );

            return donationMapper.toResponse(
                    saved
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw databaseException(
                    "Donation could not be created",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public DonationResponse updateDonation(
            Long id,
            DonationRequest request
    ) {
        Donation donation =
                findDonation(id);

        validateRequest(request);

        donation.setDonationTypeId(
                request.donationTypeId()
        );

        donation.setMemberId(
                request.memberId()
        );

        donation.setSponsorId(
                request.sponsorId()
        );

        donation.setDonorName(
                trimToNull(
                        request.donorName()
                )
        );

        donation.setActivityId(
                request.activityId()
        );

        donation.setBranchId(
                request.branchId()
        );

        donation.setDonationPeriod(
                request.donationPeriod()
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
            Donation saved =
                    donationRepository.saveAndFlush(
                            donation
                    );

            return donationMapper.toResponse(
                    saved
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw databaseException(
                    "Donation could not be updated",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void deleteDonation(
            Long id
    ) {
        Donation donation =
                findDonation(id);

        try {
            donationRepository.delete(
                    donation
            );

            donationRepository.flush();

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    getDatabaseMessage(
                            "Donation cannot be deleted because related records exist",
                            exception
                    ),
                    exception
            );
        }
    }

    private void validateRequest(
            DonationRequest request
    ) {
        requireActiveLookup(
                "donation_types",
                request.donationTypeId(),
                "Donation type"
        );

        requireActiveLookup(
                "payment_methods",
                request.paymentMethodId(),
                "Payment method"
        );

        requireExistingRecord(
                "branches",
                request.branchId(),
                "Branch"
        );

        if (request.memberId() != null) {
            requireExistingRecord(
                    "members",
                    request.memberId(),
                    "Member"
            );

            validateMemberBranch(
                    request.memberId(),
                    request.branchId()
            );
        }

        if (request.activityId() != null) {
            requireExistingRecord(
                    "activities",
                    request.activityId(),
                    "Activity"
            );

            validateActivityBranch(
                    request.activityId(),
                    request.branchId()
            );
        }

        if (request.receiptFileId() != null) {
            requireExistingRecord(
                    "files",
                    request.receiptFileId(),
                    "Receipt file"
            );
        }

        if (request.sponsorId() != null) {
            requireExistingRecord(
                    "sponsors",
                    request.sponsorId(),
                    "Sponsor"
            );
        }

        validateAmounts(
                request.amountKhr(),
                request.amountUsd()
        );

        validateDonationTypeRules(
                request
        );
    }

    private void validateDonationTypeRules(
            DonationRequest request
    ) {
        String donationTypeCode =
                getDonationTypeCode(
                        request.donationTypeId()
                );

        if (MONTHLY_DONATION.equalsIgnoreCase(
                donationTypeCode
        )) {
            if (request.memberId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Monthly donation requires a member"
                );
            }

            if (request.donationPeriod() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Monthly donation requires a donation period"
                );
            }
        }

        if (ACTIVITY_DONATION.equalsIgnoreCase(
                donationTypeCode
        )
                && request.activityId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity donation requires an activity"
            );
        }

        if (SPONSOR_DONATION.equalsIgnoreCase(
                donationTypeCode
        )
                && request.sponsorId() == null
                && trimToNull(
                request.donorName()
        ) == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Sponsor donation requires either a sponsor \
                    or a donor name
                    """
            );
        }

        boolean hasDonor =
                request.memberId() != null
                        || request.sponsorId() != null
                        || trimToNull(
                        request.donorName()
                ) != null;

        if (!hasDonor) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Donation requires a member, sponsor, \
                    or donor name
                    """
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
                    "Donation amounts cannot be negative"
            );
        }

        if (safeKhr.compareTo(BigDecimal.ZERO) == 0
                && safeUsd.compareTo(BigDecimal.ZERO) == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    At least one donation amount must be \
                    greater than zero
                    """
            );
        }
    }

    private void validateMemberBranch(
            Long memberId,
            Long branchId
    ) {
        Long memberBranchId =
                jdbcTemplate.query(
                        """
                        SELECT branch_id
                        FROM members
                        WHERE id = ?
                        """,
                        resultSet ->
                                resultSet.next()
                                        ? resultSet.getLong(
                                        "branch_id"
                                )
                                        : null,
                        memberId
                );

        if (memberBranchId != null
                && !memberBranchId.equals(branchId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    The selected member does not belong \
                    to the selected branch
                    """
            );
        }
    }

    private void validateActivityBranch(
            Long activityId,
            Long branchId
    ) {
        Long activityBranchId =
                jdbcTemplate.query(
                        """
                        SELECT branch_id
                        FROM activities
                        WHERE id = ?
                        """,
                        resultSet ->
                                resultSet.next()
                                        ? resultSet.getLong(
                                        "branch_id"
                                )
                                        : null,
                        activityId
                );

        if (activityBranchId != null
                && !activityBranchId.equals(branchId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    The selected activity does not belong \
                    to the selected branch
                    """
            );
        }
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

        String sql =
                """
                SELECT COUNT(*)
                FROM %s
                WHERE id = ?
                  AND is_active = TRUE
                """.formatted(tableName);

        Long count =
                jdbcTemplate.queryForObject(
                        sql,
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

        String sql =
                """
                SELECT COUNT(*)
                FROM %s
                WHERE id = ?
                """.formatted(tableName);

        Long count =
                jdbcTemplate.queryForObject(
                        sql,
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

    private String getDonationTypeCode(
            Short donationTypeId
    ) {
        List<String> codes =
                jdbcTemplate.query(
                        """
                        SELECT code
                        FROM donation_types
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> resultSet.getString(
                                "code"
                        ),
                        donationTypeId
                );

        if (codes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Donation type not found"
            );
        }

        return codes.getFirst();
    }

    private Donation findDonation(
            Long id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation ID is required"
            );
        }

        return donationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Donation not found with ID: "
                                        + id
                        )
                );
    }

    private String generateDonationNumber() {
        LocalDate today =
                LocalDate.now();

        String prefix =
                "DON-"
                        + today.format(
                        DateTimeFormatter.BASIC_ISO_DATE
                )
                        + "-";

        String latestDonationNumber =
                donationRepository
                        .findLatestDonationNoByPrefix(
                                prefix
                        )
                        .orElse(null);

        int nextSequence = 1;

        if (latestDonationNumber != null) {
            String sequenceText =
                    latestDonationNumber.substring(
                            prefix.length()
                    );

            try {
                nextSequence =
                        Integer.parseInt(
                                sequenceText
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

        if (username == null
                || username.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated username is unavailable"
            );
        }

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
                        ) -> resultSet.getLong(
                                "id"
                        ),
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
                || exchangeRate.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

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

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private void validateDateRange(
            OffsetDateTime paidFrom,
            OffsetDateTime paidTo
    ) {
        if (paidFrom != null
                && paidTo != null
                && paidFrom.isAfter(paidTo)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The start date cannot be after the end date"
            );
        }
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