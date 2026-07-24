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

    /*
     * ==========================================================
     * BASE DONATION QUERY
     * ==========================================================
     */

    private static final String BASE_DONATION_SQL = """
            SELECT
                d.id                              AS donation_id,
                d.donation_no                     AS donation_no,
                d.donation_period                 AS donation_period,

                d.amount_khr                      AS amount_khr,
                d.amount_usd                      AS amount_usd,
                d.exchange_rate_khr_per_usd       AS exchange_rate_khr_per_usd,
                d.total_amount_usd                AS total_amount_usd,

                d.paid_at                         AS paid_at,
                d.payment_reference               AS payment_reference,
                d.note                            AS note,

                dt.id                             AS donation_type_id,
                dt.code                           AS donation_type_code,
                dt.label_km                       AS donation_type_label_km,
                dt.label_en                       AS donation_type_label_en,

                pm.id                             AS payment_method_id,
                pm.code                           AS payment_method_code,
                pm.label_km                       AS payment_method_label_km,
                pm.label_en                       AS payment_method_label_en,

                m.id                              AS member_id,
                m.member_no                       AS member_no,
                m.full_name_km                    AS member_full_name_km,
                m.full_name_en                    AS member_full_name_en,

                b.id                              AS branch_id,
                b.name_km                         AS branch_name_km,
                b.name_en                         AS branch_name_en,

                a.id                              AS activity_id,
                a.title_km                        AS activity_title_km,
                a.title_en                        AS activity_title_en,

                rf.id                             AS receipt_file_id,
                rf.file_path                      AS receipt_file_path,
                rf.original_name                  AS receipt_original_name,
                rf.mime_type                      AS receipt_mime_type,
                rf.size_bytes                     AS receipt_size_bytes,

                d.recorded_by                     AS recorded_by_user_id,
                d.created_at                      AS created_at,
                d.updated_at                      AS updated_at

            FROM donations d

            INNER JOIN donation_types dt
                    ON dt.id = d.donation_type_id

            INNER JOIN payment_methods pm
                    ON pm.id = d.payment_method_id

            INNER JOIN members m
                    ON m.id = d.member_id

            INNER JOIN branches b
                    ON b.id = d.branch_id

            LEFT JOIN activities a
                   ON a.id = d.activity_id

            LEFT JOIN files rf
                   ON rf.id = d.receipt_file_id

            WHERE d.member_id = :memberId
            """;

    /*
     * ==========================================================
     * GET ALL MY DONATIONS
     * ==========================================================
     */

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

    /*
     * ==========================================================
     * GET ONE MY DONATION
     * ==========================================================
     */

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
            /*
             * This returns 404 whether:
             *
             * - the donation does not exist, or
             * - it belongs to another member.
             *
             * That avoids exposing another member's data.
             */
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Donation was not found for the logged-in member"
            );
        }

        return results.get(0);
    }

    /*
     * ==========================================================
     * GET MY DONATION SUMMARY
     * ==========================================================
     */

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

        /*
         * An aggregate query normally always returns one row,
         * but this protects against an unexpected null result.
         */
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

    /*
     * ==========================================================
     * ROW MAPPER
     * ==========================================================
     */

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

                resultSet.getBigDecimal(
                        "exchange_rate_khr_per_usd"
                ),

                getBigDecimalOrZero(
                        resultSet,
                        "total_amount_usd"
                ),

                resultSet.getObject(
                        "paid_at",
                        OffsetDateTime.class
                ),

                resultSet.getString(
                        "payment_reference"
                ),

                resultSet.getString(
                        "note"
                ),

                getNullableLong(
                        resultSet,
                        "donation_type_id"
                ),

                resultSet.getString(
                        "donation_type_code"
                ),

                resultSet.getString(
                        "donation_type_label_km"
                ),

                resultSet.getString(
                        "donation_type_label_en"
                ),

                getNullableLong(
                        resultSet,
                        "payment_method_id"
                ),

                resultSet.getString(
                        "payment_method_code"
                ),

                resultSet.getString(
                        "payment_method_label_km"
                ),

                resultSet.getString(
                        "payment_method_label_en"
                ),

                getNullableLong(
                        resultSet,
                        "member_id"
                ),

                resultSet.getString(
                        "member_no"
                ),

                resultSet.getString(
                        "member_full_name_km"
                ),

                resultSet.getString(
                        "member_full_name_en"
                ),

                getNullableLong(
                        resultSet,
                        "branch_id"
                ),

                resultSet.getString(
                        "branch_name_km"
                ),

                resultSet.getString(
                        "branch_name_en"
                ),

                getNullableLong(
                        resultSet,
                        "activity_id"
                ),

                resultSet.getString(
                        "activity_title_km"
                ),

                resultSet.getString(
                        "activity_title_en"
                ),

                getNullableLong(
                        resultSet,
                        "receipt_file_id"
                ),

                resultSet.getString(
                        "receipt_file_path"
                ),

                resultSet.getString(
                        "receipt_original_name"
                ),

                resultSet.getString(
                        "receipt_mime_type"
                ),

                getNullableLong(
                        resultSet,
                        "receipt_size_bytes"
                ),

                getNullableLong(
                        resultSet,
                        "recorded_by_user_id"
                ),

                resultSet.getObject(
                        "created_at",
                        OffsetDateTime.class
                ),

                resultSet.getObject(
                        "updated_at",
                        OffsetDateTime.class
                )
        );
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED MEMBER
     * ==========================================================
     */

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

        /*
         * Reload the user from the database so that the current
         * users.member_id relationship is used.
         */
        User currentUser =
                userRepository
                        .findById(
                                authenticatedUser.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found "
                                                + "in the database"
                                )
                        );

        Long memberId = currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to "
                            + "a member profile"
            );
        }

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to "
                            + "this account was not found"
            );
        }

        return memberId;
    }

    /*
     * ==========================================================
     * VALIDATION
     * ==========================================================
     */

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

    /*
     * ==========================================================
     * JDBC NULL HELPERS
     * ==========================================================
     */

    private Long getNullableLong(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        long value = resultSet.getLong(columnName);

        return resultSet.wasNull()
                ? null
                : value;
    }

    private BigDecimal getBigDecimalOrZero(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        BigDecimal value =
                resultSet.getBigDecimal(columnName);

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}