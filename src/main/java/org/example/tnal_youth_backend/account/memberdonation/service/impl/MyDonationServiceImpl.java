package org.example.tnal_youth_backend.account.memberdonation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationSummaryResponse;
import org.example.tnal_youth_backend.account.memberdonation.service.MyDonationService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDonationServiceImpl
        implements MyDonationService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    private static final String BASE_DONATION_SQL = """
            SELECT
                d.id                         AS donation_id,
                d.donation_no                AS donation_no,
                d.donation_type_id           AS donation_type_id,
                d.activity_id                AS activity_id,
                d.branch_id                  AS branch_id,
                d.donation_period            AS donation_period,
                d.amount_khr                 AS amount_khr,
                d.amount_usd                 AS amount_usd,
                d.total_amount_usd           AS total_amount_usd,
                d.payment_method_id          AS payment_method_id,
                d.paid_at                    AS paid_at,
                d.payment_reference          AS payment_reference,
                d.receipt_file_id            AS receipt_file_id,
                d.note                       AS note
            FROM donations d
            WHERE d.member_id = :memberId
            """;

    @Override
    public List<MyDonationResponse> getMyDonations() {

        Long memberId = getCurrentMemberId();

        String sql = BASE_DONATION_SQL + """

                ORDER BY
                    d.paid_at DESC,
                    d.id DESC
                """;

        return jdbcTemplate.query(
                sql,
                Map.of("memberId", memberId),
                this::mapDonation
        );
    }

    @Override
    public List<MyDonationResponse> searchByDonationPeriod(
            String period
    ) {
        String normalizedPeriod =
                trimToNull(period);

        if (normalizedPeriod == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation period is required"
            );
        }

        final YearMonth yearMonth;

        try {
            yearMonth =
                    YearMonth.parse(
                            normalizedPeriod
                    );

        } catch (
                DateTimeParseException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Donation period must use yyyy-MM format,                     for example 2026-07
                    """
            );
        }

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_DONATION_SQL + """

                AND d.donation_period
                    BETWEEN :startDate
                    AND :endDate

                ORDER BY
                    d.paid_at DESC,
                    d.id DESC
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("startDate", startDate)
                        .addValue("endDate", endDate);

        return jdbcTemplate.query(
                sql,
                parameters,
                this::mapDonation
        );
    }

    @Override
    public List<MyDonationResponse> filterByPaymentMethod(
            Short paymentMethodId
    ) {
        validatePaymentMethodId(
                paymentMethodId
        );

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_DONATION_SQL + """

                AND d.payment_method_id =
                    :paymentMethodId

                ORDER BY
                    d.paid_at DESC,
                    d.id DESC
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue(
                                "paymentMethodId",
                                paymentMethodId
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                this::mapDonation
        );
    }

    @Override
    public MyDonationResponse getMyDonationById(
            Long donationId
    ) {
        validateDonationId(donationId);

        Long memberId = getCurrentMemberId();

        String sql = BASE_DONATION_SQL + """

                AND d.id = :donationId
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("donationId", donationId);

        List<MyDonationResponse> results =
                jdbcTemplate.query(
                        sql,
                        parameters,
                        this::mapDonation
                );

        if (results.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Donation was not found for the logged-in member"
            );
        }

        return results.get(0);
    }

    @Override
    public MyDonationSummaryResponse getMyDonationSummary() {

        Long memberId = getCurrentMemberId();

        String sql = """
                SELECT
                    COUNT(*) AS total_donation_records,

                    COALESCE(
                        SUM(d.amount_khr),
                        0
                    ) AS total_amount_khr,

                    COALESCE(
                        SUM(d.amount_usd),
                        0
                    ) AS total_amount_usd,

                    COALESCE(
                        SUM(d.total_amount_usd),
                        0
                    ) AS overall_total_usd,

                    COUNT(*) FILTER (
                        WHERE dt.code = 'MONTHLY_DONATION'
                    ) AS monthly_donation_records,

                    COUNT(*) FILTER (
                        WHERE dt.code = 'ACTIVITY_DONATION'
                    ) AS activity_donation_records,

                    COUNT(*) FILTER (
                        WHERE dt.code = 'SPONSOR_DONATION'
                    ) AS sponsor_donation_records,

                    MAX(d.paid_at) AS latest_paid_at

                FROM donations d

                INNER JOIN donation_types dt
                        ON dt.id = d.donation_type_id

                WHERE d.member_id = :memberId
                """;

        MyDonationSummaryResponse response =
                jdbcTemplate.queryForObject(
                        sql,
                        Map.of("memberId", memberId),
                        (resultSet, rowNumber) ->
                                new MyDonationSummaryResponse(
                                        resultSet.getLong(
                                                "total_donation_records"
                                        ),
                                        getBigDecimalOrZero(
                                                resultSet,
                                                "total_amount_khr"
                                        ),
                                        getBigDecimalOrZero(
                                                resultSet,
                                                "total_amount_usd"
                                        ),
                                        getBigDecimalOrZero(
                                                resultSet,
                                                "overall_total_usd"
                                        ),
                                        resultSet.getLong(
                                                "monthly_donation_records"
                                        ),
                                        resultSet.getLong(
                                                "activity_donation_records"
                                        ),
                                        resultSet.getLong(
                                                "sponsor_donation_records"
                                        ),
                                        resultSet.getObject(
                                                "latest_paid_at",
                                                OffsetDateTime.class
                                        )
                                )
                );

        if (response == null) {
            return new MyDonationSummaryResponse(
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    0,
                    0,
                    null
            );
        }

        return response;
    }

    private MyDonationResponse mapDonation(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {

        return new MyDonationResponse(
                getNullableLong(
                        resultSet,
                        "donation_id"
                ),
                resultSet.getString(
                        "donation_no"
                ),
                getNullableShort(
                        resultSet,
                        "donation_type_id"
                ),
                getNullableLong(
                        resultSet,
                        "activity_id"
                ),
                getNullableLong(
                        resultSet,
                        "branch_id"
                ),
                resultSet.getObject(
                        "donation_period",
                        LocalDate.class
                ),
                getBigDecimalOrZero(
                        resultSet,
                        "amount_khr"
                ),
                getBigDecimalOrZero(
                        resultSet,
                        "amount_usd"
                ),
                getBigDecimalOrZero(
                        resultSet,
                        "total_amount_usd"
                ),
                getNullableShort(
                        resultSet,
                        "payment_method_id"
                ),
                resultSet.getObject(
                        "paid_at",
                        OffsetDateTime.class

                ),
                resultSet.getString(
                        "payment_reference"
                ),
                getNullableLong(
                        resultSet,
                        "receipt_file_id"
                ),
                resultSet.getString(
                        "note"
                )
        );
    }

    private Long getCurrentMemberId() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        User currentUser =
                userRepository
                        .findById(
                                authenticatedUser.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found in the database"
                                )
                        );

        Long memberId = currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to a member profile"
            );
        }

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to this account was not found"
            );
        }

        return memberId;
    }

    private void validatePaymentMethodId(
            Short paymentMethodId
    ) {
        if (paymentMethodId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment method ID is required"
            );
        }

        Long count =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM payment_methods
                        WHERE id = :paymentMethodId
                          AND is_active = TRUE
                        """,
                        Map.of(
                                "paymentMethodId",
                                paymentMethodId
                        ),
                        Long.class
                );

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Payment method not found or inactive with ID: "
                            + paymentMethodId
            );
        }
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

    private void validateDonationId(
            Long donationId
    ) {
        if (donationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation ID is required"
            );
        }

        if (donationId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation ID must be greater than zero"
            );
        }
    }

    private Long getNullableLong(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        Number value =
                (Number) resultSet.getObject(
                        columnName
                );

        return value == null
                ? null
                : value.longValue();
    }

    private Short getNullableShort(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        Number value =
                (Number) resultSet.getObject(
                        columnName
                );

        return value == null
                ? null
                : value.shortValue();
    }

    private BigDecimal getBigDecimalOrZero(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        BigDecimal value =
                resultSet.getBigDecimal(
                        columnName
                );

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}