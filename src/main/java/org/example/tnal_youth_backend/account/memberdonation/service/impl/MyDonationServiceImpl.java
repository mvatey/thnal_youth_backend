package org.example.tnal_youth_backend.account.memberdonation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberdonation.dto.response.MyDonationResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDonationServiceImpl
        implements MyDonationService {

    private static final String MONTHLY_DONATION =
            "MONTHLY_DONATION";

    private static final String SPONSOR_DONATION =
            "SPONSOR_DONATION";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    /*
     * My Account ownership rule:
     *
     * users.member_id
     *      -> members.id
     *      -> donations.member_id
     *
     * Both monthly and sponsor-type records are restricted to
     * the member profile linked to the logged-in account.
     */
    private static final String BASE_SQL = """
            SELECT
                donation.id
                    AS donation_id,

                donation.donation_no
                    AS donation_no,

                donation_type.id
                    AS donation_type_id,

                donation_type.code
                    AS donation_type_code,

                donation_type.label_km
                    AS donation_type_label_km,

                donation_type.label_en
                    AS donation_type_label_en,

                sponsor.id
                    AS sponsor_id,

                sponsor.sponsor_type_id
                    AS sponsor_type_id,

                sponsor_type.code
                    AS sponsor_type_code,

                sponsor_type.label_km
                    AS sponsor_type_label_km,

                sponsor_type.label_en
                    AS sponsor_type_label_en,

                sponsor.name
                    AS sponsor_name,

                sponsor.phone
                    AS sponsor_phone,

                CAST(sponsor.email AS TEXT)
                    AS sponsor_email,

                donation.donor_name
                    AS donor_name,

                activity.id
                    AS activity_id,

                activity.title_km
                    AS activity_title_km,

                activity.title_en
                    AS activity_title_en,

                branch.id
                    AS branch_id,

                branch.name_km
                    AS branch_name_km,

                branch.name_en
                    AS branch_name_en,

                donation.donation_period
                    AS donation_period,

                donation.amount_khr
                    AS amount_khr,

                donation.amount_usd
                    AS amount_usd,

                donation.total_amount_usd
                    AS total_amount_usd,

                payment_method.id
                    AS payment_method_id,

                payment_method.code
                    AS payment_method_code,

                payment_method.label_km
                    AS payment_method_label_km,

                payment_method.label_en
                    AS payment_method_label_en,

                donation.paid_at
                    AS paid_at,

                donation.payment_reference
                    AS payment_reference,

                recorded_by.id
                    AS recorded_by_id,

                recorded_by.full_name_km
                    AS recorded_by_full_name_km,

                recorded_by.full_name_en
                    AS recorded_by_full_name_en,

                receipt.id
                    AS receipt_id,

                receipt.file_path
                    AS receipt_url,

                receipt.original_name
                    AS receipt_original_name,

                receipt.mime_type
                    AS receipt_mime_type,

                receipt.size_bytes
                    AS receipt_size_bytes,

                donation.note
                    AS note

            FROM donations donation

            INNER JOIN donation_types donation_type
                    ON donation_type.id =
                       donation.donation_type_id

            LEFT JOIN sponsors sponsor
                   ON sponsor.id =
                      donation.sponsor_id

            LEFT JOIN sponsor_types sponsor_type
                   ON sponsor_type.id =
                      sponsor.sponsor_type_id

            LEFT JOIN activities activity
                   ON activity.id =
                      donation.activity_id

            INNER JOIN branches branch
                    ON branch.id =
                       donation.branch_id

            INNER JOIN payment_methods payment_method
                    ON payment_method.id =
                       donation.payment_method_id

            INNER JOIN users recorded_by
                    ON recorded_by.id =
                       donation.recorded_by

            LEFT JOIN files receipt
                   ON receipt.id =
                      donation.receipt_file_id

            WHERE donation.member_id =
                  :memberId

              AND donation_type.code =
                  :donationTypeCode
            """;

    /*
     * Event (activity) donations are identified by having a linked
     * activity, regardless of donation_type — mirrors the frontend's
     * own grouping rule (eventdonation/page.js: rows where activityId
     * is present are treated as event/program donations).
     */
    private static final String EVENT_BASE_SQL = """
            SELECT
                donation.id
                    AS donation_id,

                donation.donation_no
                    AS donation_no,

                donation_type.id
                    AS donation_type_id,

                donation_type.code
                    AS donation_type_code,

                donation_type.label_km
                    AS donation_type_label_km,

                donation_type.label_en
                    AS donation_type_label_en,

                sponsor.id
                    AS sponsor_id,

                sponsor.sponsor_type_id
                    AS sponsor_type_id,

                sponsor_type.code
                    AS sponsor_type_code,

                sponsor_type.label_km
                    AS sponsor_type_label_km,

                sponsor_type.label_en
                    AS sponsor_type_label_en,

                sponsor.name
                    AS sponsor_name,

                sponsor.phone
                    AS sponsor_phone,

                CAST(sponsor.email AS TEXT)
                    AS sponsor_email,

                donation.donor_name
                    AS donor_name,

                activity.id
                    AS activity_id,

                activity.title_km
                    AS activity_title_km,

                activity.title_en
                    AS activity_title_en,

                branch.id
                    AS branch_id,

                branch.name_km
                    AS branch_name_km,

                branch.name_en
                    AS branch_name_en,

                donation.donation_period
                    AS donation_period,

                donation.amount_khr
                    AS amount_khr,

                donation.amount_usd
                    AS amount_usd,

                donation.total_amount_usd
                    AS total_amount_usd,

                payment_method.id
                    AS payment_method_id,

                payment_method.code
                    AS payment_method_code,

                payment_method.label_km
                    AS payment_method_label_km,

                payment_method.label_en
                    AS payment_method_label_en,

                donation.paid_at
                    AS paid_at,

                donation.payment_reference
                    AS payment_reference,

                recorded_by.id
                    AS recorded_by_id,

                recorded_by.full_name_km
                    AS recorded_by_full_name_km,

                recorded_by.full_name_en
                    AS recorded_by_full_name_en,

                receipt.id
                    AS receipt_id,

                receipt.file_path
                    AS receipt_url,

                receipt.original_name
                    AS receipt_original_name,

                receipt.mime_type
                    AS receipt_mime_type,

                receipt.size_bytes
                    AS receipt_size_bytes,

                donation.note
                    AS note

            FROM donations donation

            INNER JOIN donation_types donation_type
                    ON donation_type.id =
                       donation.donation_type_id

            LEFT JOIN sponsors sponsor
                   ON sponsor.id =
                      donation.sponsor_id

            LEFT JOIN sponsor_types sponsor_type
                   ON sponsor_type.id =
                      sponsor.sponsor_type_id

            LEFT JOIN activities activity
                   ON activity.id =
                      donation.activity_id

            INNER JOIN branches branch
                    ON branch.id =
                       donation.branch_id

            INNER JOIN payment_methods payment_method
                    ON payment_method.id =
                       donation.payment_method_id

            INNER JOIN users recorded_by
                    ON recorded_by.id =
                       donation.recorded_by

            LEFT JOIN files receipt
                   ON receipt.id =
                      donation.receipt_file_id

            WHERE donation.member_id =
                  :memberId

              AND donation.activity_id IS NOT NULL

              AND donation_type.code =
                  'ACTIVITY_DONATION'

              /*
               * The activity-income editor is one current row per
               * member + activity + branch. Older duplicate rows can exist
               * from historical/test saves, but My Donations must mirror
               * the editor and expose only the latest current row.
               */
              AND donation.id = (
                  SELECT d2.id
                  FROM donations d2
                  WHERE d2.member_id = donation.member_id
                    AND d2.activity_id = donation.activity_id
                    AND d2.branch_id = donation.branch_id
                  ORDER BY
                      d2.updated_at DESC NULLS LAST,
                      d2.created_at DESC NULLS LAST,
                      d2.id DESC
                  LIMIT 1
              )
            """;

    /*
     * ==========================================================
     * MONTHLY DONATIONS
     * ==========================================================
     */

    @Override
    public List<MyDonationResponse> getMyMonthlyDonations() {

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_SQL + """

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        return jdbcTemplate.query(
                sql,
                baseParameters(
                        memberId,
                        MONTHLY_DONATION
                ),
                this::mapDonation
        );
    }

    @Override
    public List<MyDonationResponse> searchMyMonthlyDonations(
            String period
    ) {

        YearMonth yearMonth =
                parseYearMonth(
                        period
                );

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_SQL + """

                AND donation.donation_period
                    BETWEEN :startDate
                    AND :endDate

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        MapSqlParameterSource parameters =
                baseParameters(
                        memberId,
                        MONTHLY_DONATION
                )
                        .addValue(
                                "startDate",
                                startDate
                        )
                        .addValue(
                                "endDate",
                                endDate
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                this::mapDonation
        );
    }

    @Override
    public List<MyDonationResponse>
    filterMyMonthlyDonationsByPaymentMethod(
            Short paymentMethodId
    ) {

        validatePaymentMethodId(
                paymentMethodId
        );

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_SQL + """

                AND donation.payment_method_id =
                    :paymentMethodId

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        MapSqlParameterSource parameters =
                baseParameters(
                        memberId,
                        MONTHLY_DONATION
                )
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

    /*
     * ==========================================================
     * EVENT (ACTIVITY) DONATIONS
     * ==========================================================
     */

    @Override
    public List<MyDonationResponse> getMyEventDonations() {

        Long memberId =
                getCurrentMemberId();

        String sql = EVENT_BASE_SQL + """

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        return jdbcTemplate.query(
                sql,
                eventParameters(
                        memberId
                ),
                this::mapDonation
        );
    }

    /*
     * ==========================================================
     * SPONSOR DONATIONS
     * ==========================================================
     */

    @Override
    public List<MyDonationResponse> getMySponsorDonations() {

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_SQL + """

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        return jdbcTemplate.query(
                sql,
                baseParameters(
                        memberId,
                        SPONSOR_DONATION
                ),
                this::mapDonation
        );
    }

    @Override
    public List<MyDonationResponse> searchMySponsorDonations(
            String search
    ) {

        String normalizedSearch =
                trimToNull(
                        search
                );

        if (normalizedSearch == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search value is required"
            );
        }

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_SQL + """

                AND (
                    LOWER(
                        COALESCE(
                            donation.donation_no,
                            ''
                        )
                    ) LIKE LOWER(
                        CONCAT(
                            '%',
                            :search,
                            '%'
                        )
                    )

                    OR LOWER(
                        COALESCE(
                            donation.payment_reference,
                            ''
                        )
                    ) LIKE LOWER(
                        CONCAT(
                            '%',
                            :search,
                            '%'
                        )
                    )

                    OR LOWER(
                        COALESCE(
                            donation.note,
                            ''
                        )
                    ) LIKE LOWER(
                        CONCAT(
                            '%',
                            :search,
                            '%'
                        )
                    )

                    OR LOWER(
                        COALESCE(
                            sponsor.name,
                            ''
                        )
                    ) LIKE LOWER(
                        CONCAT(
                            '%',
                            :search,
                            '%'
                        )
                    )
                )

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        MapSqlParameterSource parameters =
                baseParameters(
                        memberId,
                        SPONSOR_DONATION
                )
                        .addValue(
                                "search",
                                normalizedSearch
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                this::mapDonation
        );
    }

    @Override
    public List<MyDonationResponse>
    filterMySponsorDonationsByPaymentMethod(
            Short paymentMethodId
    ) {

        validatePaymentMethodId(
                paymentMethodId
        );

        Long memberId =
                getCurrentMemberId();

        String sql = BASE_SQL + """

                AND donation.payment_method_id =
                    :paymentMethodId

                ORDER BY
                    donation.paid_at DESC,
                    donation.id DESC
                """;

        MapSqlParameterSource parameters =
                baseParameters(
                        memberId,
                        SPONSOR_DONATION
                )
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

    /*
     * ==========================================================
     * RESPONSE MAPPING
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
                new MyDonationResponse.DonationTypeInfo(
                        getNullableShort(
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
                        )
                ),
                mapSponsor(
                        resultSet
                ),
                resultSet.getString(
                        "donor_name"
                ),
                mapActivity(
                        resultSet
                ),
                mapBranch(
                        resultSet
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
                mapPaymentMethod(
                        resultSet
                ),
                resultSet.getObject(
                        "paid_at",
                        OffsetDateTime.class
                ),
                resultSet.getString(
                        "payment_reference"
                ),
                mapRecordedBy(
                        resultSet
                ),
                mapReceipt(
                        resultSet
                ),
                resultSet.getString(
                        "note"
                )
        );
    }

    private MyDonationResponse.SponsorInfo mapSponsor(
            ResultSet resultSet
    ) throws SQLException {

        Long sponsorId =
                getNullableLong(
                        resultSet,
                        "sponsor_id"
                );

        if (sponsorId == null) {
            return null;
        }

        return new MyDonationResponse.SponsorInfo(
                sponsorId,
                getNullableShort(
                        resultSet,
                        "sponsor_type_id"
                ),
                resultSet.getString(
                        "sponsor_type_code"
                ),
                resultSet.getString(
                        "sponsor_type_label_km"
                ),
                resultSet.getString(
                        "sponsor_type_label_en"
                ),
                resultSet.getString(
                        "sponsor_name"
                ),
                resultSet.getString(
                        "sponsor_phone"
                ),
                resultSet.getString(
                        "sponsor_email"
                )
        );
    }

    private MyDonationResponse.ActivityInfo mapActivity(
            ResultSet resultSet
    ) throws SQLException {

        Long activityId =
                getNullableLong(
                        resultSet,
                        "activity_id"
                );

        if (activityId == null) {
            return null;
        }

        return new MyDonationResponse.ActivityInfo(
                activityId,
                resultSet.getString(
                        "activity_title_km"
                ),
                resultSet.getString(
                        "activity_title_en"
                )
        );
    }

    private MyDonationResponse.BranchInfo mapBranch(
            ResultSet resultSet
    ) throws SQLException {

        Long branchId =
                getNullableLong(
                        resultSet,
                        "branch_id"
                );

        if (branchId == null) {
            return null;
        }

        return new MyDonationResponse.BranchInfo(
                branchId,
                resultSet.getString(
                        "branch_name_km"
                ),
                resultSet.getString(
                        "branch_name_en"
                )
        );
    }

    private MyDonationResponse.PaymentMethodInfo mapPaymentMethod(
            ResultSet resultSet
    ) throws SQLException {

        Short paymentMethodId =
                getNullableShort(
                        resultSet,
                        "payment_method_id"
                );

        if (paymentMethodId == null) {
            return null;
        }

        return new MyDonationResponse.PaymentMethodInfo(
                paymentMethodId,
                resultSet.getString(
                        "payment_method_code"
                ),
                resultSet.getString(
                        "payment_method_label_km"
                ),
                resultSet.getString(
                        "payment_method_label_en"
                )
        );
    }

    private MyDonationResponse.RecordedByInfo mapRecordedBy(
            ResultSet resultSet
    ) throws SQLException {

        Long recordedById =
                getNullableLong(
                        resultSet,
                        "recorded_by_id"
                );

        if (recordedById == null) {
            return null;
        }

        return new MyDonationResponse.RecordedByInfo(
                recordedById,
                resultSet.getString(
                        "recorded_by_full_name_km"
                ),
                resultSet.getString(
                        "recorded_by_full_name_en"
                )
        );
    }

    private MyDonationResponse.ReceiptInfo mapReceipt(
            ResultSet resultSet
    ) throws SQLException {

        Long receiptId =
                getNullableLong(
                        resultSet,
                        "receipt_id"
                );

        if (receiptId == null) {
            return null;
        }

        Long sizeBytes =
                getNullableLong(
                        resultSet,
                        "receipt_size_bytes"
                );

        return new MyDonationResponse.ReceiptInfo(
                receiptId,
                resultSet.getString(
                        "receipt_url"
                ),
                resultSet.getString(
                        "receipt_original_name"
                ),
                resultSet.getString(
                        "receipt_mime_type"
                ),
                sizeBytes,
                calculateSizeKb(
                        sizeBytes
                ),
                calculateSizeMb(
                        sizeBytes
                )
        );
    }

    /*
     * ==========================================================
     * CURRENT MEMBER
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

        Long memberId =
                currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to a member profile"
            );
        }

        if (!memberRepository.existsById(
                memberId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to this account was not found"
            );
        }

        return memberId;
    }

    /*
     * ==========================================================
     * VALIDATION AND HELPERS
     * ==========================================================
     */

    private MapSqlParameterSource eventParameters(
            Long memberId
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "memberId",
                        memberId
                );
    }

    private MapSqlParameterSource baseParameters(
            Long memberId,
            String donationTypeCode
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "memberId",
                        memberId
                )
                .addValue(
                        "donationTypeCode",
                        donationTypeCode
                );
    }

    private YearMonth parseYearMonth(
            String period
    ) {

        String normalizedPeriod =
                trimToNull(
                        period
                );

        if (normalizedPeriod == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation period is required"
            );
        }

        try {
            return YearMonth.parse(
                    normalizedPeriod
            );

        } catch (
                DateTimeParseException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Donation period must use yyyy-MM format, for example 2026-07"
            );
        }
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
                        new MapSqlParameterSource()
                                .addValue(
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

    private Double calculateSizeKb(
            Long sizeBytes
    ) {

        if (sizeBytes == null) {
            return null;
        }

        return roundToTwoDecimals(
                sizeBytes / 1024.0
        );
    }

    private Double calculateSizeMb(
            Long sizeBytes
    ) {

        if (sizeBytes == null) {
            return null;
        }

        return roundToTwoDecimals(
                sizeBytes / (1024.0 * 1024.0)
        );
    }

    private Double roundToTwoDecimals(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}