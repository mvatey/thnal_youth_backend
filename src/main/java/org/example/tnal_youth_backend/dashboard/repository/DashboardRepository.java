package org.example.tnal_youth_backend.dashboard.repository;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.dashboard.repository.projection.DonationTotals;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

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
}