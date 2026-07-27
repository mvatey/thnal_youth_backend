package org.example.tnal_youth_backend.member.branch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class BranchStaffRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Returns all active branch assignments for one member.
     *
     * Active assignment:
     * ended_on IS NULL
     */
    public Set<Long> findActiveBranchIdsByMemberId(
            Long memberId
    ) {
        if (memberId == null) {
            return Set.of();
        }

        String sql = """
                SELECT DISTINCT
                    bs.branch_id
                FROM branch_staff bs
                WHERE bs.member_id = :memberId
                  AND bs.ended_on IS NULL
                ORDER BY bs.branch_id
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "memberId",
                                memberId
                        );

        List<Long> branchIds =
                jdbcTemplate.queryForList(
                        sql,
                        parameters,
                        Long.class
                );

        return new LinkedHashSet<>(
                branchIds
        );
    }

    /**
     * Checks whether a member currently has access
     * to a specific branch.
     */
    public boolean existsActiveAssignment(
            Long memberId,
            Long branchId
    ) {
        if (memberId == null
                || branchId == null) {
            return false;
        }

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM branch_staff bs
                    WHERE bs.member_id = :memberId
                      AND bs.branch_id = :branchId
                      AND bs.ended_on IS NULL
                )
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue(
                                "memberId",
                                memberId
                        )
                        .addValue(
                                "branchId",
                                branchId
                        );

        Boolean result =
                jdbcTemplate.queryForObject(
                        sql,
                        parameters,
                        Boolean.class
                );

        return Boolean.TRUE.equals(result);
    }
}