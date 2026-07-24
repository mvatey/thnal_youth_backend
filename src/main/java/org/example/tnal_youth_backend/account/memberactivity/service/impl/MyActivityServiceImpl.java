package org.example.tnal_youth_backend.account.memberactivity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberactivity.dto.response.MyActivityResponse;
import org.example.tnal_youth_backend.account.memberactivity.dto.response.MyActivitySummaryResponse;
import org.example.tnal_youth_backend.account.memberactivity.service.MyActivityService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings({
        "SqlDialectInspection",
        "SqlNoDataSourceInspection",
        "SqlResolve",
        "DuplicatedCode"
})
public class MyActivityServiceImpl
        implements MyActivityService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    /*
     * Your current activity_participants table does not contain:
     *
     * - is_invited
     * - joined_at
     *
     * Therefore:
     *
     * - invited is temporarily returned as false
     * - joinedAt is temporarily returned as null
     */

    // language=PostgreSQL
    private static final String BASE_ACTIVITY_SQL = """
            SELECT
                activity.id                       AS activity_id,
                activity.title_km                 AS title_km,
                activity.title_en                 AS title_en,
                activity.description              AS description,
                activity.starts_at                AS starts_at,
                activity.ends_at                  AS ends_at,
                activity.location_name            AS location_name,
                activity.address                  AS address,
                activity.is_public                AS is_public,
                activity.capacity                 AS capacity,

                activity_type.id                  AS activity_type_id,
                activity_type.code                AS activity_type_code,
                activity_type.label_km            AS activity_type_label_km,
                activity_type.label_en            AS activity_type_label_en,

                activity_sector.id                AS activity_sector_id,
                activity_sector.code              AS activity_sector_code,
                activity_sector.label_km          AS activity_sector_label_km,
                activity_sector.label_en          AS activity_sector_label_en,

                activity_status.id                AS activity_status_id,
                activity_status.code              AS activity_status_code,
                activity_status.label_km          AS activity_status_label_km,
                activity_status.label_en          AS activity_status_label_en,

                branch.id                         AS branch_id,
                branch.name_km                    AS branch_name_km,
                branch.name_en                    AS branch_name_en,

                participant.id                    AS participant_id,

                FALSE                             AS is_invited,

                CAST(NULL AS TIMESTAMPTZ)          AS joined_at,

                attendance_status.id              AS attendance_status_id,
                attendance_status.code            AS attendance_status_code,
                attendance_status.label_km        AS attendance_status_label_km,
                attendance_status.label_en        AS attendance_status_label_en,

                cover_file.id                     AS cover_image_id,
                cover_file.file_path              AS cover_image_path,
                cover_file.original_name          AS cover_image_original_name

            FROM activity_participants participant

            INNER JOIN activities activity
                    ON activity.id = participant.activity_id

            LEFT JOIN activity_types activity_type
                   ON activity_type.id = activity.type_id

            LEFT JOIN activity_sectors activity_sector
                   ON activity_sector.id = activity.sector_id

            LEFT JOIN activity_statuses activity_status
                   ON activity_status.id = activity.status_id

            LEFT JOIN branches branch
                   ON branch.id = activity.branch_id

            LEFT JOIN attendance_statuses attendance_status
                   ON attendance_status.id =
                      participant.attendance_status_id

            LEFT JOIN files cover_file
                   ON cover_file.id = activity.cover_image_id

            WHERE participant.member_id = :memberId
            """;

    // language=PostgreSQL
    private static final String GET_ALL_ACTIVITIES_SQL =
            BASE_ACTIVITY_SQL + """

                    ORDER BY
                        activity.starts_at DESC NULLS LAST,
                        activity.id DESC
                    """;

    // language=PostgreSQL
    private static final String GET_ONE_ACTIVITY_SQL =
            BASE_ACTIVITY_SQL + """

                    AND activity.id = :activityId
                    """;

    /*
     * invited_activities is currently zero because the
     * activity_participants table does not have is_invited.
     */

    // language=PostgreSQL
    private static final String ACTIVITY_SUMMARY_SQL = """
            SELECT
                COUNT(*) AS total_activities,

                CAST(0 AS BIGINT) AS invited_activities,

                COUNT(*) FILTER (
                    WHERE attendance_status.code = 'PRESENT'
                ) AS attended_activities,

                COUNT(*) FILTER (
                    WHERE attendance_status.code = 'ABSENT'
                ) AS absent_activities,

                COUNT(*) FILTER (
                    WHERE participant.attendance_status_id IS NULL
                       OR attendance_status.code NOT IN (
                              'PRESENT',
                              'ABSENT'
                          )
                ) AS pending_activities

            FROM activity_participants participant

            LEFT JOIN attendance_statuses attendance_status
                   ON attendance_status.id =
                      participant.attendance_status_id

            WHERE participant.member_id = :memberId
            """;

    private final RowMapper<MyActivityResponse> activityRowMapper =
            this::mapActivity;

    /*
     * ==========================================================
     * GET ALL MY ACTIVITIES
     * ==========================================================
     */

    @Override
    public List<MyActivityResponse> getMyActivities() {

        Long memberId = getCurrentMemberId();

        return jdbcTemplate.query(
                GET_ALL_ACTIVITIES_SQL,
                memberParameters(memberId),
                activityRowMapper
        );
    }

    /*
     * ==========================================================
     * GET ONE MY ACTIVITY
     * ==========================================================
     */

    @Override
    public MyActivityResponse getMyActivityById(
            Long activityId
    ) {
        validateActivityId(activityId);

        Long memberId = getCurrentMemberId();

        MapSqlParameterSource parameters =
                memberParameters(memberId)
                        .addValue(
                                "activityId",
                                activityId
                        );

        List<MyActivityResponse> activities =
                jdbcTemplate.query(
                        GET_ONE_ACTIVITY_SQL,
                        parameters,
                        activityRowMapper
                );

        if (activities.isEmpty()) {
            /*
             * Returns 404 when:
             *
             * - the activity does not exist; or
             * - the activity exists but the logged-in member
             *   is not in activity_participants.
             */
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Activity was not found for the logged-in member"
            );
        }

        return activities.getFirst();
    }

    /*
     * ==========================================================
     * GET MY ACTIVITY SUMMARY
     * ==========================================================
     */

    @Override
    public MyActivitySummaryResponse getMyActivitySummary() {

        Long memberId = getCurrentMemberId();

        MyActivitySummaryResponse summary =
                jdbcTemplate.queryForObject(
                        ACTIVITY_SUMMARY_SQL,
                        memberParameters(memberId),
                        (resultSet, rowNumber) ->
                                new MyActivitySummaryResponse(
                                        resultSet.getLong(
                                                "total_activities"
                                        ),
                                        resultSet.getLong(
                                                "invited_activities"
                                        ),
                                        resultSet.getLong(
                                                "attended_activities"
                                        ),
                                        resultSet.getLong(
                                                "absent_activities"
                                        ),
                                        resultSet.getLong(
                                                "pending_activities"
                                        )
                                )
                );

        return summary != null
                ? summary
                : emptySummary();
    }

    /*
     * ==========================================================
     * MAP DATABASE ROW TO RESPONSE
     * ==========================================================
     */

    private MyActivityResponse mapActivity(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {

        return new MyActivityResponse(
                getNullableLong(
                        resultSet,
                        "activity_id"
                ),

                resultSet.getString(
                        "title_km"
                ),

                resultSet.getString(
                        "title_en"
                ),

                resultSet.getString(
                        "description"
                ),

                getNullableOffsetDateTime(
                        resultSet,
                        "starts_at"
                ),

                getNullableOffsetDateTime(
                        resultSet,
                        "ends_at"
                ),

                resultSet.getString(
                        "location_name"
                ),

                resultSet.getString(
                        "address"
                ),

                getNullableBoolean(
                        resultSet,
                        "is_public"
                ),

                getNullableInteger(
                        resultSet,
                        "capacity"
                ),

                getNullableLong(
                        resultSet,
                        "activity_type_id"
                ),

                resultSet.getString(
                        "activity_type_code"
                ),

                resultSet.getString(
                        "activity_type_label_km"
                ),

                resultSet.getString(
                        "activity_type_label_en"
                ),

                getNullableLong(
                        resultSet,
                        "activity_sector_id"
                ),

                resultSet.getString(
                        "activity_sector_code"
                ),

                resultSet.getString(
                        "activity_sector_label_km"
                ),

                resultSet.getString(
                        "activity_sector_label_en"
                ),

                getNullableLong(
                        resultSet,
                        "activity_status_id"
                ),

                resultSet.getString(
                        "activity_status_code"
                ),

                resultSet.getString(
                        "activity_status_label_km"
                ),

                resultSet.getString(
                        "activity_status_label_en"
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
                        "participant_id"
                ),

                getNullableBoolean(
                        resultSet,
                        "is_invited"
                ),

                getNullableOffsetDateTime(
                        resultSet,
                        "joined_at"
                ),

                getNullableLong(
                        resultSet,
                        "attendance_status_id"
                ),

                resultSet.getString(
                        "attendance_status_code"
                ),

                resultSet.getString(
                        "attendance_status_label_km"
                ),

                resultSet.getString(
                        "attendance_status_label_en"
                ),

                getNullableLong(
                        resultSet,
                        "cover_image_id"
                ),

                resultSet.getString(
                        "cover_image_path"
                ),

                resultSet.getString(
                        "cover_image_original_name"
                )
        );
    }

    /*
     * ==========================================================
     * GET CURRENT LOGGED-IN MEMBER ID
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
         * Reload the user from the database so the current
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

        Long memberId =
                currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to a member profile"
            );
        }

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to this account "
                            + "was not found"
            );
        }

        return memberId;
    }

    /*
     * ==========================================================
     * SQL PARAMETERS
     * ==========================================================
     */

    private MapSqlParameterSource memberParameters(
            Long memberId
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "memberId",
                        memberId
                );
    }

    /*
     * ==========================================================
     * VALIDATION
     * ==========================================================
     */

    private void validateActivityId(
            Long activityId
    ) {
        if (activityId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is required"
            );
        }

        if (activityId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID must be greater than zero"
            );
        }
    }

    /*
     * ==========================================================
     * SAFE DATABASE TYPE CONVERSION
     * ==========================================================
     */

    /*
     * PostgreSQL IDs may use:
     *
     * int2  -> Short
     * int4  -> Integer
     * int8  -> Long
     *
     * Reading all of them as Number and calling longValue()
     * prevents:
     *
     * conversion to class java.lang.Long from int2 not supported
     */
    private Long getNullableLong(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        Object rawValue =
                resultSet.getObject(columnName);

        if (rawValue == null) {
            return null;
        }

        if (!(rawValue instanceof Number number)) {
            throw new SQLException(
                    "Column "
                            + columnName
                            + " does not contain a numeric value"
            );
        }

        return number.longValue();
    }

    private Integer getNullableInteger(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        Object rawValue =
                resultSet.getObject(columnName);

        if (rawValue == null) {
            return null;
        }

        if (!(rawValue instanceof Number number)) {
            throw new SQLException(
                    "Column "
                            + columnName
                            + " does not contain a numeric value"
            );
        }

        return number.intValue();
    }

    private Boolean getNullableBoolean(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        Object rawValue =
                resultSet.getObject(columnName);

        if (rawValue == null) {
            return null;
        }

        if (rawValue instanceof Boolean booleanValue) {
            return booleanValue;
        }

        throw new SQLException(
                "Column "
                        + columnName
                        + " does not contain a boolean value"
        );
    }

    private OffsetDateTime getNullableOffsetDateTime(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {

        return resultSet.getObject(
                columnName,
                OffsetDateTime.class
        );
    }

    /*
     * ==========================================================
     * EMPTY SUMMARY
     * ==========================================================
     */

    private MyActivitySummaryResponse emptySummary() {

        return new MyActivitySummaryResponse(
                0,
                0,
                0,
                0,
                0
        );
    }
}