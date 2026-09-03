package org.example.tnal_youth_backend.member.branch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.util.Optional;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchLeaderResponse;

@Repository
@RequiredArgsConstructor
public class BranchStaffRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<BranchLeaderResponse> findActiveLeader(Long branchId) {
        String sql = """
                SELECT m.id, m.full_name_km, m.full_name_en, m.gender,
                       ms.code AS status, m.phone, m.email, m.date_of_birth,
                       m.joined_on, f.id AS profile_photo_id, f.file_path AS profile_image
                FROM branch_staff bs
                JOIN positions p ON p.id = bs.position_id AND p.code = 'BRANCH_LEADER'
                JOIN members m ON m.id = bs.member_id
                JOIN member_statuses ms ON ms.id = m.status_id
                LEFT JOIN files f ON f.id = m.profile_photo_id
                WHERE bs.branch_id = :branchId
                  AND bs.ended_on IS NULL
                  AND bs.is_primary = TRUE
                ORDER BY bs.started_on DESC, bs.id DESC
                LIMIT 1
                """;
        List<BranchLeaderResponse> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("branchId", branchId),
                (rs, rowNum) -> new BranchLeaderResponse(
                        rs.getLong("id"), rs.getString("full_name_km"),
                        rs.getString("full_name_en"), rs.getString("gender"),
                        rs.getString("status"), rs.getString("phone"),
                        rs.getString("email"), rs.getObject("date_of_birth", LocalDate.class),
                        rs.getObject("joined_on", LocalDate.class),
                        rs.getObject("profile_photo_id", Long.class), rs.getString("profile_image"),
                        "BRANCH_LEADER"
                )
        );
        return rows.stream().findFirst();
    }


    public Optional<Long> findActiveLeaderBranchIdByMemberId(Long memberId) {
        if (memberId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT bs.branch_id
                FROM branch_staff bs
                JOIN positions p ON p.id = bs.position_id
                WHERE bs.member_id = :memberId
                  AND p.code = 'BRANCH_LEADER'
                  AND bs.ended_on IS NULL
                  AND bs.is_primary = TRUE
                ORDER BY bs.started_on DESC, bs.id DESC
                LIMIT 1
                """;

        List<Long> rows = jdbcTemplate.queryForList(
                sql,
                new MapSqlParameterSource("memberId", memberId),
                Long.class
        );

        return rows.stream().findFirst();
    }

    public boolean isActiveMemberOfBranch(Long branchId, Long memberId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM members m
                    JOIN member_statuses ms ON ms.id = m.status_id
                    WHERE m.id = :memberId AND m.branch_id = :branchId AND ms.code = 'ACTIVE'
                )
                """;
        Boolean result = jdbcTemplate.queryForObject(sql,
                new MapSqlParameterSource().addValue("branchId", branchId).addValue("memberId", memberId),
                Boolean.class);
        return Boolean.TRUE.equals(result);
    }


    public void endOtherActiveAssignmentsForLeader(Long memberId, Long keepBranchId) {
        if (memberId == null || keepBranchId == null) {
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    is_primary = FALSE,
                    updated_at = NOW()
                WHERE member_id = :memberId
                  AND branch_id <> :keepBranchId
                  AND ended_on IS NULL
                """,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("keepBranchId", keepBranchId)
        );
    }

    public void assignLeader(Long branchId, Long memberId, Long appointedBy) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("branchId", branchId).addValue("memberId", memberId)
                .addValue("appointedBy", appointedBy);
        jdbcTemplate.update("""
                UPDATE branch_staff SET ended_on = CURRENT_DATE, is_primary = FALSE, updated_at = NOW()
                WHERE branch_id = :branchId AND ended_on IS NULL AND is_primary = TRUE
                  AND position_id = (SELECT id FROM positions WHERE code = 'BRANCH_LEADER')
                  AND member_id <> :memberId
                """, params);

        // The previous leader's branch_staff row just ended above, but their
        // login role was never reset -- without this they keep BRANCH_LEADER
        // access (and show up as a leader in places that read users.role)
        // even though branch_staff no longer records them as one.
        demoteStaleLeaderRoles();

        int updated = jdbcTemplate.update("""
                UPDATE branch_staff SET is_primary = TRUE, appointed_by = :appointedBy, updated_at = NOW()
                WHERE branch_id = :branchId AND member_id = :memberId AND ended_on IS NULL
                  AND position_id = (SELECT id FROM positions WHERE code = 'BRANCH_LEADER')
                """, params);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO branch_staff(branch_id, member_id, position_id, started_on, is_primary, appointed_by)
                    VALUES (:branchId, :memberId,
                            (SELECT id FROM positions WHERE code = 'BRANCH_LEADER'),
                            CURRENT_DATE, TRUE, :appointedBy)
                    """, params);
        }
        jdbcTemplate.update("""
                UPDATE users SET role = 'BRANCH_LEADER', updated_at = NOW()
                WHERE member_id = :memberId AND role <> 'ADMIN'
                """, params);
    }

    /**
     * Demotes any login account still marked BRANCH_LEADER back to MEMBER
     * once it no longer holds an active primary branch_staff leadership row
     * anywhere -- the same cleanup {@link #removeLeader} already does,
     * pulled out so {@link #assignLeader} can run it too right after ending
     * someone's leadership there.
     */
    public void demoteStaleLeaderRoles() {
        jdbcTemplate.update("""
                UPDATE users u SET role = 'MEMBER', updated_at = NOW()
                WHERE u.role = 'BRANCH_LEADER'
                  AND NOT EXISTS (
                    SELECT 1 FROM branch_staff bs JOIN positions p ON p.id = bs.position_id
                    WHERE bs.member_id = u.member_id AND p.code = 'BRANCH_LEADER'
                      AND bs.ended_on IS NULL AND bs.is_primary = TRUE
                  )
                """, new MapSqlParameterSource());
    }

    public void removeLeader(Long branchId) {
        MapSqlParameterSource params = new MapSqlParameterSource("branchId", branchId);
        jdbcTemplate.update("""
                UPDATE branch_staff SET ended_on = CURRENT_DATE, is_primary = FALSE, updated_at = NOW()
                WHERE branch_id = :branchId AND ended_on IS NULL AND is_primary = TRUE
                  AND position_id = (SELECT id FROM positions WHERE code = 'BRANCH_LEADER')
                """, params);
        demoteStaleLeaderRoles();
    }

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

        return new LinkedHashSet<>(branchIds);
    }

    /**
     * The user IDs of a branch's active leadership (branch leader/
     * secretary) — either via an explicit active branch_staff assignment,
     * or (for staff who never got one) via their member record's home
     * branch. Symmetric with the fallback used in
     * {@code resolveStaffBranchIds} elsewhere in the codebase. Used to
     * notify a branch's leadership of things like a new co-hosting
     * invitation to an activity.
     */
    public Set<Long> findActiveStaffUserIds(Long branchId) {
        if (branchId == null) {
            return Set.of();
        }

        String sql = """
                SELECT DISTINCT u.id
                FROM users u
                JOIN members m ON m.id = u.member_id
                WHERE u.role IN ('BRANCH_LEADER', 'SECRETARY')
                  AND (
                        m.branch_id = :branchId
                        OR EXISTS (
                            SELECT 1
                            FROM branch_staff bs
                            WHERE bs.member_id = m.id
                              AND bs.branch_id = :branchId
                              AND bs.ended_on IS NULL
                        )
                  )
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId);

        List<Long> userIds =
                jdbcTemplate.queryForList(sql, parameters, Long.class);

        return new LinkedHashSet<>(userIds);
    }

    /**
     * Inserts a new active, non-primary branch_staff assignment for a
     * freshly created member. Always non-primary: primary is reserved for
     * the single branch-leader assignment managed by
     * {@link #assignLeader}, and {@code uq_branch_staff_member_single_primary}
     * allows at most one active primary assignment per member across the
     * whole system.
     */
    public void assignPosition(
            Long branchId,
            Long memberId,
            Short positionId,
            LocalDate startedOn,
            Long appointedBy
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO branch_staff(branch_id, member_id, position_id, started_on, is_primary, appointed_by)
                VALUES (:branchId, :memberId, :positionId, :startedOn, FALSE, :appointedBy)
                """,
                new MapSqlParameterSource()
                        .addValue("branchId", branchId)
                        .addValue("memberId", memberId)
                        .addValue("positionId", positionId)
                        .addValue("startedOn", startedOn != null ? startedOn : LocalDate.now())
                        .addValue("appointedBy", appointedBy)
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
