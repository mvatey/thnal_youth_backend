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
import org.example.tnal_youth_backend.dashboard.repository.projection.DashboardBranchRow;

import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // =========================================================
    // MEMBERS
    // =========================================================

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

        return queryCount(sql, parameters);
    }

    public long countActiveMembersByBranchesBefore(
            Collection<Long> branchIds,
            LocalDate exclusiveEndDate
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT COUNT(*)
                FROM members m
                JOIN member_statuses ms
                    ON ms.id = m.status_id
                WHERE ms.code = 'ACTIVE'
                  AND m.branch_id IN (:branchIds)
                  AND COALESCE(
                        m.joined_on,
                        m.created_at::date
                  ) < :exclusiveEndDate
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchIds", branchIds)
                        .addValue(
                                "exclusiveEndDate",
                                exclusiveEndDate
                        );

        return queryCount(sql, parameters);
    }

    // =========================================================
    // BRANCHES
    // =========================================================

    public long countAllActiveBranches() {
        String sql = """
                SELECT COUNT(*)
                FROM branches b
                JOIN branch_statuses bs
                    ON bs.id = b.status_id
                WHERE bs.code = 'ACTIVE'
                """;

        return queryCount(
                sql,
                new MapSqlParameterSource()
        );
    }

    public long countActiveBranchesByIds(
            Collection<Long> branchIds
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT COUNT(*)
                FROM branches b
                JOIN branch_statuses bs
                    ON bs.id = b.status_id
                WHERE bs.code = 'ACTIVE'
                  AND b.id IN (:branchIds)
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchIds", branchIds);

        return queryCount(sql, parameters);
    }

    // =========================================================
    // ACTIVITY SUMMARY
    // =========================================================

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
                        .addValue(
                                "exclusiveEnd",
                                exclusiveEnd
                        );

        return queryCount(sql, parameters);
    }

    public long countActivitiesByBranchesBefore(
            Collection<Long> branchIds,
            OffsetDateTime exclusiveEnd
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT COUNT(*)
                FROM activities a
                WHERE a.branch_id IN (:branchIds)
                  AND a.starts_at < :exclusiveEnd
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchIds", branchIds)
                        .addValue(
                                "exclusiveEnd",
                                exclusiveEnd
                        );

        return queryCount(sql, parameters);
    }

    // =========================================================
    // DONATION SUMMARY
    // =========================================================

    public DonationTotals sumAllDonationsBetween(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        String sql = """
                SELECT
                    COALESCE(
                        SUM(d.amount_khr),
                        0
                    ) AS amount_khr,
                    COALESCE(
                        SUM(d.amount_usd),
                        0
                    ) AS amount_usd
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

        return queryDonationTotals(
                sql,
                parameters
        );
    }

    public DonationTotals sumDonationsByBranchesBetween(
            Collection<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT
                    COALESCE(
                        SUM(d.amount_khr),
                        0
                    ) AS amount_khr,
                    COALESCE(
                        SUM(d.amount_usd),
                        0
                    ) AS amount_usd
                FROM donations d
                WHERE d.branch_id IN (:branchIds)
                  AND d.paid_at >= :startInclusive
                  AND d.paid_at < :endExclusive
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchIds", branchIds)
                        .addValue(
                                "startInclusive",
                                startInclusive
                        )
                        .addValue(
                                "endExclusive",
                                endExclusive
                        );

        return queryDonationTotals(
                sql,
                parameters
        );
    }

    // =========================================================
    // RECENT COMPLETED ACTIVITIES
    // =========================================================

    public List<DashboardActivityRow>
    findRecentCompletedActivities() {

        String sql = """
                SELECT
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id AS cover_image_id,
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
                LEFT JOIN activity_participants ap
                    ON ap.activity_id = a.id
                WHERE ast.code = 'COMPLETED'
                GROUP BY
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id,
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
    findRecentCompletedActivitiesByBranches(
            Collection<Long> branchIds
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id AS cover_image_id,
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
                LEFT JOIN activity_participants ap
                    ON ap.activity_id = a.id
                WHERE ast.code = 'COMPLETED'
                  AND a.branch_id IN (:branchIds)
                GROUP BY
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id,
                    a.starts_at,
                    a.ends_at,
                    a.location_name,
                    at.code
                ORDER BY a.ends_at DESC
                LIMIT 5
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "branchIds",
                                branchIds
                        );

        return jdbcTemplate.query(
                sql,
                parameters,
                activityRowMapper()
        );
    }

    // =========================================================
    // UPCOMING ACTIVITIES
    // =========================================================

    public List<DashboardActivityRow>
    findUpcomingActivities(
            OffsetDateTime now
    ) {
        String sql = """
                SELECT
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id AS cover_image_id,
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
                LEFT JOIN activity_participants ap
                    ON ap.activity_id = a.id
                WHERE ast.code = 'UPCOMING'
                  AND a.starts_at >= :now
                GROUP BY
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id,
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
    findUpcomingActivitiesByBranches(
            Collection<Long> branchIds,
            OffsetDateTime now
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id AS cover_image_id,
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
                LEFT JOIN activity_participants ap
                    ON ap.activity_id = a.id
                WHERE ast.code = 'UPCOMING'
                  AND a.branch_id IN (:branchIds)
                  AND a.starts_at >= :now
                GROUP BY
                    a.id,
                    a.title_km,
                    a.title_en,
                    a.cover_image_id,
                    a.starts_at,
                    a.ends_at,
                    a.location_name,
                    at.code
                ORDER BY a.starts_at ASC
                LIMIT 5
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "branchIds",
                                branchIds
                        )
                        .addValue("now", now);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityRowMapper()
        );
    }

    // =========================================================
    // ACTIVITY TYPE BREAKDOWN
    // =========================================================

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
                ORDER BY at.code
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
    findActivityTypeBreakdownByBranches(
            Collection<Long> branchIds,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT
                    at.code,
                    COUNT(*) AS total
                FROM activities a
                JOIN activity_types at
                    ON at.id = a.type_id
                WHERE a.branch_id IN (:branchIds)
                  AND a.starts_at >= :start
                  AND a.starts_at < :end
                GROUP BY at.code
                ORDER BY at.code
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "branchIds",
                                branchIds
                        )
                        .addValue("start", start)
                        .addValue("end", end);

        return jdbcTemplate.query(
                sql,
                parameters,
                activityTypeMapper()
        );
    }

    // =========================================================
    // PARTICIPATION TREND
    // =========================================================

    public List<MonthlyParticipationRow>
    findParticipationTrend(
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        String sql = """
                SELECT
                    EXTRACT(
                        MONTH FROM a.starts_at
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
    findParticipationTrendByBranches(
            Collection<Long> branchIds,
            OffsetDateTime start,
            OffsetDateTime end
    ) {
        requireBranchIds(branchIds);

        String sql = """
                SELECT
                    EXTRACT(
                        MONTH FROM a.starts_at
                    )::int AS month,
                    COUNT(ap.id) AS participation_count
                FROM activities a
                JOIN activity_participants ap
                    ON ap.activity_id = a.id
                WHERE a.branch_id IN (:branchIds)
                  AND a.starts_at >= :start
                  AND a.starts_at < :end
                GROUP BY month
                ORDER BY month
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "branchIds",
                                branchIds
                        )
                        .addValue("start", start)
                        .addValue("end", end);

        return jdbcTemplate.query(
                sql,
                parameters,
                participationTrendMapper()
        );
    }

    // =========================================================
    // SHARED HELPERS
    // =========================================================

    private long queryCount(
            String sql,
            MapSqlParameterSource parameters
    ) {
        Long result =
                jdbcTemplate.queryForObject(
                        sql,
                        parameters,
                        Long.class
                );

        return result == null
                ? 0L
                : result;
    }

    private DonationTotals queryDonationTotals(
            String sql,
            MapSqlParameterSource parameters
    ) {
        DonationTotals result =
                jdbcTemplate.queryForObject(
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

    private BigDecimal safeDecimal(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private void requireBranchIds(
            Collection<Long> branchIds
    ) {
        if (branchIds == null
                || branchIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one accessible branch ID is required."
            );
        }
    }

    private RowMapper<DashboardActivityRow>
    activityRowMapper() {
        return (resultSet, rowNumber) ->
                new DashboardActivityRow(
                        resultSet.getLong("id"),
                        resultSet.getString(
                                "title_km"
                        ),
                        resultSet.getString(
                                "title_en"
                        ),
                        resultSet.getObject(
                                "cover_image_id",
                                Long.class
                        ),
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

    private RowMapper<ActivityTypeCountRow>
    activityTypeMapper() {
        return (resultSet, rowNumber) ->
                new ActivityTypeCountRow(
                        resultSet.getString(
                                "code"
                        ),
                        resultSet.getLong(
                                "total"
                        )
                );
    }

    private RowMapper<MonthlyParticipationRow>
    participationTrendMapper() {
        return (resultSet, rowNumber) ->
                new MonthlyParticipationRow(
                        resultSet.getInt(
                                "month"
                        ),
                        resultSet.getLong(
                                "participation_count"
                        )
                );
    }

    public long countAllActivitiesBetween(
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        String sql = """
            SELECT COUNT(*)
            FROM activities a
            WHERE a.starts_at >= :startInclusive
              AND a.starts_at < :endExclusive
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

        return queryCount(sql, parameters);
    }

    public long countActivitiesByBranchesBetween(
            Collection<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        requireBranchIds(branchIds);

        String sql = """
            SELECT COUNT(*)
            FROM activities a
            WHERE a.branch_id IN (:branchIds)
              AND a.starts_at >= :startInclusive
              AND a.starts_at < :endExclusive
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "branchIds",
                                branchIds
                        )
                        .addValue(
                                "startInclusive",
                                startInclusive
                        )
                        .addValue(
                                "endExclusive",
                                endExclusive
                        );

        return queryCount(sql, parameters);
    }

    public Optional<DashboardBranchRow>
    findBranchById(
            Long branchId
    ) {
        String sql = """
            SELECT
                b.id,
                b.name_km,
                b.name_en
            FROM branches b
            WHERE b.id = :branchId
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "branchId",
                                branchId
                        );

        List<DashboardBranchRow> rows =
                jdbcTemplate.query(
                        sql,
                        parameters,
                        (resultSet, rowNumber) ->
                                new DashboardBranchRow(
                                        resultSet.getLong(
                                                "id"
                                        ),
                                        resultSet.getString(
                                                "name_km"
                                        ),
                                        resultSet.getString(
                                                "name_en"
                                        )
                                )
                );

        return rows.stream().findFirst();
    }


}