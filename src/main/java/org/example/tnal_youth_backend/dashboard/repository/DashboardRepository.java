package org.example.tnal_youth_backend.dashboard.repository;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.repository.projection.ActivityTypeCountRow;
import org.example.tnal_youth_backend.dashboard.repository.projection.DashboardActivityRow;
import org.example.tnal_youth_backend.dashboard.repository.projection.DonationTotals;
import org.example.tnal_youth_backend.dashboard.repository.projection.MonthlyParticipationRow;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /*
     * =========================================================
     * MEMBERS
     * =========================================================
     */

    public long countAllActiveMembersBefore(
            LocalDate exclusiveEndDate
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM members m
                JOIN member_statuses ms
                    ON ms.id = m.status_id
                WHERE ms.code = 'ACTIVE'
                  AND COALESCE(
                        m.joined_on,
                        m.created_at::date
                  ) < :exclusiveEndDate
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "exclusiveEndDate",
                                exclusiveEndDate
                        );

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    public long countActiveMembersByBranchBefore(
            Long branchId,
            LocalDate exclusiveEndDate
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM members m
                JOIN member_statuses ms
                    ON ms.id = m.status_id
                WHERE ms.code = 'ACTIVE'
                  AND m.branch_id = :branchId
                  AND COALESCE(
                        m.joined_on,
                        m.created_at::date
                  ) < :exclusiveEndDate
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue(
                                "exclusiveEndDate",
                                exclusiveEndDate
                        );

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    /*
     * =========================================================
     * BRANCHES
     * =========================================================
     */

    public long countAllActiveBranches() {
        String sql = """
                SELECT COUNT(*)
                FROM branches b
                JOIN branch_statuses bs
                    ON bs.id = b.status_id
                WHERE bs.code = 'ACTIVE'
                """;

        Long result = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource(),
                Long.class
        );

        return result == null ? 0L : result;
    }

    public long countActiveBranchById(Long branchId) {
        String sql = """
                SELECT COUNT(*)
                FROM branches b
                JOIN branch_statuses bs
                    ON bs.id = b.status_id
                WHERE bs.code = 'ACTIVE'
                  AND b.id = :branchId
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId);

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    /*
     * =========================================================
     * ACTIVITIES
     * =========================================================
     */

    public long countAllActivitiesBefore(
            OffsetDateTime exclusiveEnd
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM activities a
                WHERE a.starts_at < :exclusiveEnd
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("exclusiveEnd", exclusiveEnd);

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    public long countActivitiesByBranchBefore(
            Long branchId,
            OffsetDateTime exclusiveEnd
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM activities a
                WHERE a.branch_id = :branchId
                  AND a.starts_at < :exclusiveEnd
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue("exclusiveEnd", exclusiveEnd);

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    /*
     * =========================================================
     * DONATIONS
     * =========================================================
     */

    public DonationTotals sumAllDonationsBetween(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        String sql = """
                SELECT
                    COALESCE(SUM(d.amount_khr), 0) AS amount_khr,
                    COALESCE(SUM(d.amount_usd), 0) AS amount_usd
                FROM donations d
                WHERE d.paid_at >= :startInclusive
                  AND d.paid_at < :endExclusive
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "startInclusive",
                                startInclusive
                        )
                        .addValue(
                                "endExclusive",
                                endExclusive
                        );

        return queryDonationTotals(sql, parameters);
    }

    public DonationTotals sumDonationsByBranchBetween(
            Long branchId,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        String sql = """
                SELECT
                    COALESCE(SUM(d.amount_khr), 0) AS amount_khr,
                    COALESCE(SUM(d.amount_usd), 0) AS amount_usd
                FROM donations d
                WHERE d.branch_id = :branchId
                  AND d.paid_at >= :startInclusive
                  AND d.paid_at < :endExclusive
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue(
                                "startInclusive",
                                startInclusive
                        )
                        .addValue(
                                "endExclusive",
                                endExclusive
                        );

        return queryDonationTotals(sql, parameters);
    }

    private DonationTotals queryDonationTotals(
            String sql,
            MapSqlParameterSource parameters
    ) {
        DonationTotals result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                (resultSet, rowNumber) ->
                        new DonationTotals(
                                safeDecimal(
                                        resultSet.getBigDecimal(
                                                "amount_khr"
                                        )
                                ),
                                safeDecimal(
                                        resultSet.getBigDecimal(
                                                "amount_usd"
                                        )
                                )
                        )
        );

        return result == null
                ? DonationTotals.zero()
                : result;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    public List<DashboardActivityRow>
    findRecentCompletedActivities() {

        String sql = """
            SELECT
                a.id,
                a.title_km,
                a.title_en,
                f.file_path AS cover_image,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code AS activity_type,
                COUNT(ap.id) AS participant_count
            FROM activities a
            JOIN activity_statuses ast
                ON ast.id = a.status_id
            JOIN activity_types at
                ON at.id = a.type_id
            LEFT JOIN files f
                ON f.id = a.cover_image_id
            LEFT JOIN activity_participants ap
                ON ap.activity_id = a.id
            WHERE ast.code = 'COMPLETED'
            GROUP BY
                a.id,
                a.title_km,
                a.title_en,
                f.file_path,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code
            ORDER BY a.ends_at DESC
            LIMIT 5
            """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(),
                activityRowMapper()
        );
    }

    public List<DashboardActivityRow>
    findRecentCompletedActivitiesByBranch(
            Long branchId
    ) {

        String sql = """
            SELECT
                a.id,
                a.title_km,
                a.title_en,
                f.file_path AS cover_image,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code AS activity_type,
                COUNT(ap.id) AS participant_count
            FROM activities a
            JOIN activity_statuses ast
                ON ast.id = a.status_id
            JOIN activity_types at
                ON at.id = a.type_id
            LEFT JOIN files f
                ON f.id = a.cover_image_id
            LEFT JOIN activity_participants ap
                ON ap.activity_id = a.id
            WHERE ast.code = 'COMPLETED'
              AND a.branch_id = :branchId
            GROUP BY
                a.id,
                a.title_km,
                a.title_en,
                f.file_path,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code
            ORDER BY a.ends_at DESC
            LIMIT 5
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityRowMapper()
        );
    }

    public List<DashboardActivityRow>
    findUpcomingActivities(
            OffsetDateTime now
    ) {

        String sql = """
            SELECT
                a.id,
                a.title_km,
                a.title_en,
                f.file_path AS cover_image,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code AS activity_type,
                COUNT(ap.id) AS participant_count
            FROM activities a
            JOIN activity_statuses ast
                ON ast.id = a.status_id
            JOIN activity_types at
                ON at.id = a.type_id
            LEFT JOIN files f
                ON f.id = a.cover_image_id
            LEFT JOIN activity_participants ap
                ON ap.activity_id = a.id
            WHERE ast.code = 'UPCOMING'
              AND a.starts_at >= :now
            GROUP BY
                a.id,
                a.title_km,
                a.title_en,
                f.file_path,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code
            ORDER BY a.starts_at ASC
            LIMIT 5
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("now", now);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityRowMapper()
        );
    }

    public List<DashboardActivityRow>
    findUpcomingActivitiesByBranch(
            Long branchId,
            OffsetDateTime now
    ) {

        String sql = """
            SELECT
                a.id,
                a.title_km,
                a.title_en,
                f.file_path AS cover_image,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code AS activity_type,
                COUNT(ap.id) AS participant_count
            FROM activities a
            JOIN activity_statuses ast
                ON ast.id = a.status_id
            JOIN activity_types at
                ON at.id = a.type_id
            LEFT JOIN files f
                ON f.id = a.cover_image_id
            LEFT JOIN activity_participants ap
                ON ap.activity_id = a.id
            WHERE ast.code = 'UPCOMING'
              AND a.branch_id = :branchId
              AND a.starts_at >= :now
            GROUP BY
                a.id,
                a.title_km,
                a.title_en,
                f.file_path,
                a.starts_at,
                a.ends_at,
                a.location_name,
                at.code
            ORDER BY a.starts_at ASC
            LIMIT 5
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue("now", now);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityRowMapper()
        );
    }

    private RowMapper<DashboardActivityRow>
    activityRowMapper() {

        return (resultSet, rowNumber) ->
                new DashboardActivityRow(
                        resultSet.getLong("id"),
                        resultSet.getString("title_km"),
                        resultSet.getString("title_en"),
                        resultSet.getString("cover_image"),
                        resultSet.getObject(
                                "starts_at",
                                OffsetDateTime.class
                        ),
                        resultSet.getObject(
                                "ends_at",
                                OffsetDateTime.class
                        ),
                        resultSet.getString(
                                "location_name"
                        ),
                        resultSet.getString(
                                "activity_type"
                        ),
                        resultSet.getLong(
                                "participant_count"
                        )
                );
    }

    public List<ActivityTypeCountRow>
    findActivityTypeBreakdown(
            OffsetDateTime start,
            OffsetDateTime end
    ) {

        String sql = """
        SELECT
            at.code,
            COUNT(*) AS total
        FROM activities a
        JOIN activity_types at
            ON at.id = a.type_id
        WHERE a.starts_at >= :start
          AND a.starts_at < :end
        GROUP BY at.code
        """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("start", start)
                        .addValue("end", end);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityTypeMapper()
        );
    }

    public List<ActivityTypeCountRow>
    findActivityTypeBreakdownByBranch(
            Long branchId,
            OffsetDateTime start,
            OffsetDateTime end
    ) {

        String sql = """
        SELECT
            at.code,
            COUNT(*) AS total
        FROM activities a
        JOIN activity_types at
            ON at.id = a.type_id
        WHERE a.branch_id = :branchId
          AND a.starts_at >= :start
          AND a.starts_at < :end
        GROUP BY at.code
        """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue("start", start)
                        .addValue("end", end);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityTypeMapper()
        );
    }

    private RowMapper<ActivityTypeCountRow>
    activityTypeMapper() {

        return (rs, rowNum) ->
                new ActivityTypeCountRow(
                        rs.getString("code"),
                        rs.getLong("total")
                );
    }

    public List<MonthlyParticipationRow>
    findParticipationTrend(
            OffsetDateTime start,
            OffsetDateTime end
    ) {

        String sql = """
        SELECT
            EXTRACT(
                MONTH
                FROM a.starts_at
            )::int AS month,

            COUNT(ap.id) AS participation_count

        FROM activities a

        JOIN activity_participants ap
            ON ap.activity_id = a.id

        WHERE a.starts_at >= :start
          AND a.starts_at < :end

        GROUP BY month

        ORDER BY month
        """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("start", start)
                        .addValue("end", end);

        return jdbcTemplate.query(
                sql,
                parameters,
                participationTrendMapper()
        );
    }

    public List<MonthlyParticipationRow>
    findParticipationTrendByBranch(
            Long branchId,
            OffsetDateTime start,
            OffsetDateTime end
    ) {

        String sql = """
        SELECT
            EXTRACT(
                MONTH
                FROM a.starts_at
            )::int AS month,

            COUNT(ap.id) AS participation_count

        FROM activities a

        JOIN activity_participants ap
            ON ap.activity_id = a.id

        WHERE a.branch_id = :branchId
          AND a.starts_at >= :start
          AND a.starts_at < :end

        GROUP BY month

        ORDER BY month
        """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue("start", start)
                        .addValue("end", end);

        return jdbcTemplate.query(
                sql,
                parameters,
                participationTrendMapper()
        );
    }

    private RowMapper<MonthlyParticipationRow>
    participationTrendMapper() {

        return (rs, rowNum) ->
                new MonthlyParticipationRow(
                        rs.getInt("month"),
                        rs.getLong("participation_count")
                );
    }
}