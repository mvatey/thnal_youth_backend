package org.example.tnal_youth_backend.donation.sponsor.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.donation.sponsor.dto.request.SponsorRequest;
import org.example.tnal_youth_backend.donation.sponsor.dto.response.SponsorResponse;
import org.example.tnal_youth_backend.donation.sponsor.entity.Sponsor;
import org.example.tnal_youth_backend.donation.sponsor.mapper.SponsorMapper;
import org.example.tnal_youth_backend.donation.sponsor.repository.SponsorRepository;
import org.example.tnal_youth_backend.donation.sponsor.service.SponsorService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SponsorServiceImpl
        implements SponsorService {

    private final SponsorRepository sponsorRepository;
    private final SponsorMapper sponsorMapper;
    private final JdbcTemplate jdbcTemplate;

    /*
     * ==========================================================
     * GET ALL SPONSORS
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<SponsorResponse> getAllSponsors() {

        return sponsorRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(sponsorMapper::toResponse)
                .toList();
    }

    /*
     * ==========================================================
     * GET ACTIVE SPONSORS
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<SponsorResponse> getActiveSponsors() {

        return sponsorRepository
                .findAllByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(sponsorMapper::toResponse)
                .toList();
    }

    /*
     * ==========================================================
     * GET SPONSOR BY ID
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public SponsorResponse getSponsorById(
            Long id
    ) {
        Sponsor sponsor =
                findSponsor(id);

        return sponsorMapper.toResponse(
                sponsor
        );
    }

    /*
     * ==========================================================
     * CREATE SPONSOR
     * ==========================================================
     */

    @Override
    @Transactional
    public SponsorResponse createSponsor(
            SponsorRequest request
    ) {
        validateSponsorType(
                request.sponsorTypeId()
        );

        String name =
                normalizeRequired(
                        request.name(),
                        "Sponsor name"
                );

        String phone =
                trimToNull(
                        request.phone()
                );

        String email =
                normalizeEmail(
                        request.email()
                );

        validateUniqueFields(
                name,
                phone,
                email,
                null
        );

        Sponsor sponsor =
                Sponsor.builder()
                        .sponsorTypeId(
                                request.sponsorTypeId()
                        )
                        .name(name)
                        .phone(phone)
                        .email(email)
                        .address(
                                trimToNull(
                                        request.address()
                                )
                        )
                        .note(
                                trimToNull(
                                        request.note()
                                )
                        )
                        .isActive(
                                request.isActive()
                        )
                        .createdById(
                                getCurrentUserId()
                        )
                        .build();

        try {
            Sponsor savedSponsor =
                    sponsorRepository.saveAndFlush(
                            sponsor
                    );

            return sponsorMapper.toResponse(
                    savedSponsor
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw createDatabaseException(
                    "Sponsor could not be created",
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * UPDATE SPONSOR
     * ==========================================================
     */

    @Override
    @Transactional
    public SponsorResponse updateSponsor(
            Long id,
            SponsorRequest request
    ) {
        Sponsor sponsor =
                findSponsor(id);

        validateSponsorType(
                request.sponsorTypeId()
        );

        String name =
                normalizeRequired(
                        request.name(),
                        "Sponsor name"
                );

        String phone =
                trimToNull(
                        request.phone()
                );

        String email =
                normalizeEmail(
                        request.email()
                );

        validateUniqueFields(
                name,
                phone,
                email,
                id
        );

        sponsor.setSponsorTypeId(
                request.sponsorTypeId()
        );

        sponsor.setName(
                name
        );

        sponsor.setPhone(
                phone
        );

        sponsor.setEmail(
                email
        );

        sponsor.setAddress(
                trimToNull(
                        request.address()
                )
        );

        sponsor.setNote(
                trimToNull(
                        request.note()
                )
        );

        sponsor.setIsActive(
                request.isActive()
        );

        try {
            Sponsor savedSponsor =
                    sponsorRepository.saveAndFlush(
                            sponsor
                    );

            return sponsorMapper.toResponse(
                    savedSponsor
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw createDatabaseException(
                    "Sponsor could not be updated",
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * DELETE SPONSOR
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteSponsor(
            Long id
    ) {
        Sponsor sponsor =
                findSponsor(id);

        Long donationCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM donations
                        WHERE sponsor_id = ?
                        """,
                        Long.class,
                        id
                );

        if (donationCount != null
                && donationCount > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    This sponsor cannot be deleted because \
                    donation records already reference it. \
                    Set the sponsor to inactive instead.
                    """
            );
        }

        try {
            sponsorRepository.delete(
                    sponsor
            );

            sponsorRepository.flush();

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    getDatabaseMessage(
                            """
                            This sponsor cannot be deleted because \
                            related records already exist
                            """,
                            exception
                    ),
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * FIND SPONSOR
     * ==========================================================
     */

    private Sponsor findSponsor(
            Long id
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor ID is required"
            );
        }

        return sponsorRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Sponsor not found with ID: "
                                        + id
                        )
                );
    }

    /*
     * ==========================================================
     * VALIDATE SPONSOR TYPE
     * ==========================================================
     */

    private void validateSponsorType(
            Short sponsorTypeId
    ) {
        if (sponsorTypeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sponsor type ID is required"
            );
        }

        Long count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM sponsor_types
                        WHERE id = ?
                          AND is_active = TRUE
                        """,
                        Long.class,
                        sponsorTypeId
                );

        if (count == null
                || count == 0) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Sponsor type not found or inactive with ID: "
                            + sponsorTypeId
            );
        }
    }

    /*
     * ==========================================================
     * UNIQUE VALIDATION
     * ==========================================================
     */

    private void validateUniqueFields(
            String name,
            String phone,
            String email,
            Long currentId
    ) {
        boolean duplicateName;

        if (currentId == null) {
            duplicateName =
                    sponsorRepository
                            .existsByNameIgnoreCase(
                                    name
                            );
        } else {
            duplicateName =
                    sponsorRepository
                            .existsByNameIgnoreCaseAndIdNot(
                                    name,
                                    currentId
                            );
        }

        if (duplicateName) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sponsor name already exists: "
                            + name
            );
        }

        if (phone != null) {
            boolean duplicatePhone;

            if (currentId == null) {
                duplicatePhone =
                        sponsorRepository
                                .existsByPhone(
                                        phone
                                );
            } else {
                duplicatePhone =
                        sponsorRepository
                                .existsByPhoneAndIdNot(
                                        phone,
                                        currentId
                                );
            }

            if (duplicatePhone) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Sponsor phone already exists: "
                                + phone
                );
            }
        }

        if (email != null) {
            boolean duplicateEmail;

            if (currentId == null) {
                duplicateEmail =
                        sponsorRepository
                                .existsByEmailIgnoreCase(
                                        email
                                );
            } else {
                duplicateEmail =
                        sponsorRepository
                                .existsByEmailIgnoreCaseAndIdNot(
                                        email,
                                        currentId
                                );
            }

            if (duplicateEmail) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Sponsor email already exists: "
                                + email
                );
            }
        }
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED USER
     * ==========================================================
     *
     * The JWT principal may be CustomUserDetails instead of
     * the User entity. Therefore, use authentication.getName()
     * and find the user by phone or email.
     */

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

    /*
     * ==========================================================
     * TEXT NORMALIZATION
     * ==========================================================
     */

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String normalizeEmail(
            String email
    ) {
        String normalizedEmail =
                trimToNull(email);

        if (normalizedEmail == null) {
            return null;
        }

        return normalizedEmail.toLowerCase(
                Locale.ROOT
        );
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

    /*
     * ==========================================================
     * DATABASE ERROR HANDLING
     * ==========================================================
     */

    private ResponseStatusException
    createDatabaseException(
            String defaultMessage,
            DataIntegrityViolationException exception
    ) {
        String message =
                getDatabaseMessage(
                        defaultMessage,
                        exception
                );

        String normalizedMessage =
                message.toLowerCase(
                        Locale.ROOT
                );

        HttpStatus status =
                normalizedMessage.contains(
                        "duplicate"
                )
                        || normalizedMessage.contains(
                        "unique"
                )
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