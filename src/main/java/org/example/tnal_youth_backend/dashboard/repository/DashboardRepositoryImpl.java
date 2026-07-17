package org.example.tnal_youth_backend.dashboard.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<Long> findRootBranchId(Long userId) {
        String sql = """
                SELECT m.branch_id
                FROM users u
                INNER JOIN members m
                    ON m.id = u.member_id
                WHERE u.id = :userId
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource("userId", userId);

        List<Long> results = jdbcTemplate.query(
                sql,
                parameters,
                (resultSet, rowNumber) ->
                        resultSet.getLong("branch_id")
        );

        return results.stream().findFirst();
    }

    @Override
    public List<Long> findAccessibleBranchIds(Long rootBranchId) {
        String sql = """
                WITH RECURSIVE branch_tree AS (
                    SELECT
                        b.id,
                        b.parent_branch_id
                    FROM branches b
                    WHERE b.id = :rootBranchId

                    UNION

                    SELECT
                        child.id,
                        child.parent_branch_id
                    FROM branches child
                    INNER JOIN branch_tree parent
                        ON child.parent_branch_id = parent.id
                )
                SELECT id
                FROM branch_tree
                ORDER BY id
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource(
                        "rootBranchId",
                        rootBranchId
                );

        return jdbcTemplate.query(
                sql,
                parameters,
                (resultSet, rowNumber) ->
                        resultSet.getLong("id")
        );
    }

    @Override
    public long countMembersUntil(
            List<Long> branchIds,
            LocalDate endExclusive
    ) {
        if (branchIds == null || branchIds.isEmpty()) {
            return 0L;
        }

        String sql = """
                SELECT COUNT(m.id)
                FROM members m
                WHERE m.branch_id IN (:branchIds)
                  AND COALESCE(
                        m.joined_on,
                        CAST(m.created_at AS DATE)
                      ) < :endExclusive
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchIds", branchIds)
                        .addValue("endExclusive", endExclusive);

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    @Override
    public long countActivitiesUntil(
            List<Long> branchIds,
            OffsetDateTime endExclusive
    ) {
        if (branchIds == null || branchIds.isEmpty()) {
            return 0L;
        }

        String sql = """
                SELECT COUNT(a.activity_id)
                FROM activities a
                WHERE a.branch_id IN (:branchIds)
                  AND a.starts_at < :endExclusive
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchIds", branchIds)
                        .addValue("endExclusive", endExclusive);

        Long result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return result == null ? 0L : result;
    }

    @Override
    public BigDecimal sumDonationsKhrBetween(
            List<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        if (branchIds == null || branchIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String sql = """
                SELECT COALESCE(SUM(d.amount_khr), 0)
                FROM donations d
                WHERE d.branch_id IN (:branchIds)
                  AND d.paid_at >= :startInclusive
                  AND d.paid_at < :endExclusive
                """;

        MapSqlParameterSource parameters =
                donationParameters(
                        branchIds,
                        startInclusive,
                        endExclusive
                );

        BigDecimal result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                BigDecimal.class
        );

        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public BigDecimal sumDonationsUsdBetween(
            List<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        if (branchIds == null || branchIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String sql = """
                SELECT COALESCE(SUM(d.amount_usd), 0)
                FROM donations d
                WHERE d.branch_id IN (:branchIds)
                  AND d.paid_at >= :startInclusive
                  AND d.paid_at < :endExclusive
                """;

        MapSqlParameterSource parameters =
                donationParameters(
                        branchIds,
                        startInclusive,
                        endExclusive
                );

        BigDecimal result = jdbcTemplate.queryForObject(
                sql,
                parameters,
                BigDecimal.class
        );

        return result == null ? BigDecimal.ZERO : result;
    }

    private MapSqlParameterSource donationParameters(
            List<Long> branchIds,
            OffsetDateTime startInclusive,
            OffsetDateTime endExclusive
    ) {
        return new MapSqlParameterSource()
                .addValue("branchIds", branchIds)
                .addValue("startInclusive", startInclusive)
                .addValue("endExclusive", endExclusive);
    }
}