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

    /**
     * Every active branch leader for one branch -- a branch may now have
     * more than one (see assignLeader below), so this returns all of
     * them rather than picking just the most recent.
     */
    public List<BranchLeaderResponse> findActiveLeaders(Long branchId) {
        String sql = """
                SELECT m.id, m.full_name_km, m.full_name_en, m.gender,
                       ms.code AS status, m.phone, m.email, m.date_of_birth,
                       m.joined_on, f.id AS profile_photo_id, f.file_path AS profile_image
                FROM branch_staff bs
                JOIN positions p ON p.id = bs.position_id AND p.mapped_role = 'BRANCH_LEADER'
                JOIN members m ON m.id = bs.member_id
                JOIN member_statuses ms ON ms.id = m.status_id
                LEFT JOIN files f ON f.id = m.profile_photo_id
                WHERE bs.branch_id = :branchId
                  AND bs.ended_on IS NULL
                  AND bs.is_primary = TRUE
                ORDER BY bs.started_on ASC, bs.id ASC
                """;
        return jdbcTemplate.query(
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
                  AND p.mapped_role = 'BRANCH_LEADER'
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


    /**
     * Ends every active branch_staff assignment a member holds, primary
     * or not -- used when their role changes away from BRANCH_LEADER or
     * SECRETARY, the two roles branch_staff tracks coverage for, so a
     * stale row never outlives the role that justified it (a leader's
     * own leadership row, or a secretary's additional-branch coverage).
     */
    public void endAllActiveAssignments(Long memberId) {
        if (memberId == null) {
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    is_primary = FALSE,
                    updated_at = NOW()
                WHERE member_id = :memberId
                  AND ended_on IS NULL
                """,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
        );
    }

    /**
     * Same as {@link #endAllActiveAssignments}, except it leaves any row
     * whose position maps to VIEWER untouched -- used when promoting to
     * VIEWER specifically. The personal-info endpoint (a separate request
     * in the same multi-step save, always called before this one) is what
     * writes that row in the first place via the generic non-primary
     * position slot (see MemberPersonalInfoServiceImpl#updatePosition) --
     * ending every active row here regardless would wipe out whichever
     * specific VIEWER-mapped position was just chosen, the same class of
     * bug endActivePrimaryAssignment already fixed for the
     * BRANCH_LEADER -> SECRETARY lateral move.
     */
    public void endAllActiveAssignmentsExceptViewerPositions(Long memberId) {
        if (memberId == null) {
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    is_primary = FALSE,
                    updated_at = NOW()
                WHERE member_id = :memberId
                  AND ended_on IS NULL
                  AND position_id NOT IN (
                      SELECT id FROM positions WHERE mapped_role = 'VIEWER'
                  )
                """,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
        );
    }

    /**
     * Ends only a member's active PRIMARY (leader) row, leaving any
     * non-primary rows untouched -- used when a BRANCH_LEADER moves
     * laterally to SECRETARY rather than being demoted to MEMBER. That
     * member still needs branch_staff coverage as a secretary, and the
     * personal-info/branches endpoints that create or keep those
     * non-primary rows run as separate requests in the same multi-step
     * save (sometimes before this one, sometimes after) -- ending
     * everything here the way endAllActiveAssignments does would wipe
     * out a row those other endpoints already wrote, or one they're
     * about to write, depending on call order.
     */
    public void endActivePrimaryAssignment(Long memberId) {
        if (memberId == null) {
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    is_primary = FALSE,
                    updated_at = NOW()
                WHERE member_id = :memberId
                  AND ended_on IS NULL
                  AND is_primary = TRUE
                """,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
        );
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

    /**
     * Adds memberId as an active branch leader of branchId, alongside any
     * other current leaders -- a branch may have more than one now, so
     * this no longer ends anyone else's leadership here. The only
     * remaining exclusivity is per-member (see uq_branch_staff_member_
     * single_primary and BranchServiceImpl#assignLeader's own check): one
     * member can't simultaneously lead two DIFFERENT branches, but a
     * branch can freely have several different members leading it.
     *
     * positionId picks WHICH leader-mapped position this is recorded
     * under (e.g. a "deputy" position distinct from the canonical
     * "ប្រធានសាខា" one, both mapped_role = BRANCH_LEADER) -- null falls
     * back to the canonical code='BRANCH_LEADER' position, for callers
     * (like a plain role-dropdown promotion) that don't have a specific
     * one in hand. If this member already actively holds some OTHER
     * leader-mapped position on this branch, that row is updated in
     * place rather than inserting a second one, which would violate
     * uq_branch_staff_member_single_primary (at most one active primary
     * row per member, full stop).
     */
    public void assignLeader(Long branchId, Long memberId, Short positionId, Long appointedBy) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("branchId", branchId).addValue("memberId", memberId)
                .addValue("positionId", positionId)
                .addValue("appointedBy", appointedBy);

        int updated = jdbcTemplate.update("""
                UPDATE branch_staff
                SET position_id = COALESCE(
                        :positionId,
                        (SELECT id FROM positions WHERE code = 'BRANCH_LEADER')
                    ),
                    is_primary = TRUE,
                    appointed_by = :appointedBy,
                    updated_at = NOW()
                WHERE branch_id = :branchId AND member_id = :memberId AND ended_on IS NULL
                  AND position_id IN (
                      SELECT id FROM positions WHERE mapped_role = 'BRANCH_LEADER'
                  )
                """, params);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO branch_staff(branch_id, member_id, position_id, started_on, is_primary, appointed_by)
                    VALUES (:branchId, :memberId,
                            COALESCE(
                                :positionId,
                                (SELECT id FROM positions WHERE code = 'BRANCH_LEADER')
                            ),
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
                    WHERE bs.member_id = u.member_id AND p.mapped_role = 'BRANCH_LEADER'
                      AND bs.ended_on IS NULL AND bs.is_primary = TRUE
                  )
                """, new MapSqlParameterSource());
    }

    /**
     * Ends one specific leader's leadership of one branch. Needs memberId
     * now that a branch can have more than one active leader -- there's
     * no longer a single implicit "the leader" to remove.
     */
    public void removeLeader(Long branchId, Long memberId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("branchId", branchId)
                .addValue("memberId", memberId);
        jdbcTemplate.update("""
                UPDATE branch_staff SET ended_on = CURRENT_DATE, is_primary = FALSE, updated_at = NOW()
                WHERE branch_id = :branchId AND member_id = :memberId
                  AND ended_on IS NULL AND is_primary = TRUE
                  AND position_id IN (
                      SELECT id FROM positions WHERE mapped_role = 'BRANCH_LEADER'
                  )
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
     * The position currently held by a member's active branch_staff
     * assignment to one branch, primary (branch leader) or not -- what a
     * "current position" display (the personal-info page, the member list
     * column) should show, since a member holding either kind has exactly
     * one active position on that branch in practice. Not for writes --
     * see {@link #findActiveNonPrimaryPositionId}, which write paths use
     * instead so they never touch the leader row by accident.
     */
    public Optional<Short> findActivePositionId(
            Long memberId,
            Long branchId
    ) {
        if (memberId == null || branchId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT bs.position_id
                FROM branch_staff bs
                WHERE bs.member_id = :memberId
                  AND bs.branch_id = :branchId
                  AND bs.ended_on IS NULL
                ORDER BY bs.is_primary DESC, bs.started_on DESC, bs.id DESC
                LIMIT 1
                """;

        List<Short> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("branchId", branchId),
                (rs, rowNum) -> rs.getShort("position_id")
        );

        return rows.stream().findFirst();
    }

    /**
     * The position currently held by a member's active, non-primary
     * branch_staff assignment to one branch -- e.g. the job title shown on
     * their personal-info page. Excludes primary assignments (the branch
     * leader row), which are a separate concept managed by
     * {@link #assignLeader}/{@link #removeLeader}, not this generic slot.
     */
    public Optional<Short> findActiveNonPrimaryPositionId(
            Long memberId,
            Long branchId
    ) {
        if (memberId == null || branchId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT bs.position_id
                FROM branch_staff bs
                WHERE bs.member_id = :memberId
                  AND bs.branch_id = :branchId
                  AND bs.ended_on IS NULL
                  AND bs.is_primary = FALSE
                ORDER BY bs.started_on DESC, bs.id DESC
                LIMIT 1
                """;

        List<Short> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("branchId", branchId),
                (rs, rowNum) -> rs.getShort("position_id")
        );

        return rows.stream().findFirst();
    }

    /**
     * Sets a member's position on one branch, independent of the branch
     * leader slot -- updates the existing active non-primary row if one
     * exists, otherwise inserts a fresh one (same shape as
     * {@link #assignPosition}, just idempotent so the personal-info page
     * can call it on every save regardless of whether this is the first
     * position the member has ever held there).
     */
    public void upsertNonPrimaryPosition(
            Long branchId,
            Long memberId,
            Short positionId,
            Long appointedBy
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("branchId", branchId)
                .addValue("memberId", memberId)
                .addValue("positionId", positionId)
                .addValue("appointedBy", appointedBy);

        int updated = jdbcTemplate.update("""
                UPDATE branch_staff
                SET position_id = :positionId, appointed_by = :appointedBy, updated_at = NOW()
                WHERE branch_id = :branchId AND member_id = :memberId
                  AND ended_on IS NULL AND is_primary = FALSE
                """, params);

        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO branch_staff(branch_id, member_id, position_id, started_on, is_primary, appointed_by)
                    VALUES (:branchId, :memberId, :positionId, CURRENT_DATE, FALSE, :appointedBy)
                    """, params);
        }
    }

    /**
     * Ends a member's active non-primary position on one branch -- used
     * when the personal-info page clears the position field back to
     * "none". Never touches the primary (branch leader) row.
     */
    public void clearNonPrimaryPosition(
            Long memberId,
            Long branchId
    ) {
        jdbcTemplate.update("""
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE, updated_at = NOW()
                WHERE member_id = :memberId AND branch_id = :branchId
                  AND ended_on IS NULL AND is_primary = FALSE
                """,
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("branchId", branchId)
        );
    }

    /**
     * The member IDs with an active (non-primary or primary, either way)
     * branch_staff assignment to this branch -- e.g. a secretary staffing
     * a second branch beyond their own members.branch_id. Used to extend
     * "members of branch X" queries so a multi-branch secretary shows up
     * everywhere their primary-branch_id counterpart already would.
     */
    public Set<Long> findMemberIdsByBranchId(
            Long branchId
    ) {
        if (branchId == null) {
            return Set.of();
        }

        String sql = """
                SELECT DISTINCT bs.member_id
                FROM branch_staff bs
                WHERE bs.branch_id = :branchId
                  AND bs.ended_on IS NULL
                """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("branchId", branchId);

        List<Long> memberIds =
                jdbcTemplate.queryForList(sql, parameters, Long.class);

        return new LinkedHashSet<>(memberIds);
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
