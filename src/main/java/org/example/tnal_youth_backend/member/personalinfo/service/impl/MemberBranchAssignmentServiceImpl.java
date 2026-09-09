package org.example.tnal_youth_backend.member.personalinfo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberBranchAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
 * Assigns/removes ADDITIONAL branches for a secretary account — the
 * member's primary/home branch stays driven by members.branch_id and
 * the existing branch field/save flow; this only manages the extra
 * branch_staff rows already surfaced as "assigned_branches" on the
 * personal-info response.
 *
 * Deliberately self-contained: it talks to branch_staff directly via
 * JDBC instead of extending BranchStaffRepository, so it doesn't
 * depend on that class's other (unrelated) internals.
 */
@Service
@RequiredArgsConstructor
public class MemberBranchAssignmentServiceImpl
        implements MemberBranchAssignmentService {

    private final MemberRepository memberRepository;

    private final UserRepository userRepository;

    private final BranchRepository branchRepository;

    private final MemberAccessValidator memberAccessValidator;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void assignBranch(
            Long memberId,
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        memberAccessValidator
                .validateCanAssignBranch(
                        memberId,
                        branchId
                );

        requireSecretaryAccount(memberId);

        if (!branchRepository.existsById(branchId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Branch not found with ID: "
                            + branchId
            );
        }

        Member member =
                requireMember(memberId);

        /*
         * A member with no home branch yet gets this one -- otherwise
         * there would be no way to give a brand-new secretary a first
         * branch through this multiselect at all, since removing the
         * member's only branch is deliberately blocked elsewhere. This
         * is purely about members.branchId (their own home/login
         * branch); it has no bearing on branch_staff.is_primary, which
         * secretaries never use -- every branch they cover is just
         * coverage, none of them exclusive to this member.
         */
        if (member.getBranchId() == null) {
            member.setBranchId(branchId);

            memberRepository
                    .saveAndFlush(member);
            synchronizeLinkedUserPrimaryBranch(memberId, branchId);
        }

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "branchId",
                                branchId
                        )
                        .addValue(
                                "memberId",
                                memberId
                        )
                        .addValue(
                                "appointedBy",
                                currentUserId()
                        );

        int reactivated = jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = NULL,
                    is_primary = FALSE,
                    updated_at = NOW()
                WHERE branch_id = :branchId
                  AND member_id = :memberId
                  AND position_id = (
                      SELECT id FROM positions
                      WHERE code = 'SECRETARY'
                  )
                  AND ended_on IS NOT NULL
                """,
                params
        );

        if (reactivated > 0) {
            return;
        }

        Boolean alreadyActive = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1 FROM branch_staff
                    WHERE branch_id = :branchId
                      AND member_id = :memberId
                      AND position_id = (
                          SELECT id FROM positions
                          WHERE code = 'SECRETARY'
                      )
                      AND ended_on IS NULL
                )
                """,
                params,
                Boolean.class
        );

        if (Boolean.TRUE.equals(alreadyActive)) {
            return;
        }

        jdbcTemplate.update(
                """
                INSERT INTO branch_staff(
                    branch_id, member_id, position_id,
                    started_on, is_primary, appointed_by
                )
                VALUES (
                    :branchId, :memberId,
                    (SELECT id FROM positions WHERE code = 'SECRETARY'),
                    CURRENT_DATE, FALSE, :appointedBy
                )
                """,
                params
        );
    }

    @Override
    @Transactional
    public void replaceBranches(
            Long memberId,
            List<Long> branchIds
    ) {
        if (branchIds == null || branchIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one branch is required"
            );
        }

        LinkedHashSet<Long> desiredBranchIds = new LinkedHashSet<>(branchIds);
        if (desiredBranchIds.size() != branchIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate branch IDs are not allowed"
            );
        }

        Member member = requireMember(memberId);
        requireSecretaryAccount(memberId);

        // Validate every requested destination against the ACTOR's scope.
        // Admin is unrestricted; Branch Leader is restricted by
        // StaffBranchScopeService through validateCanAssignBranch().
        for (Long branchId : desiredBranchIds) {
            if (branchId == null || branchId <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Branch ID must be greater than zero"
                );
            }

            memberAccessValidator.validateCanAssignBranch(memberId, branchId);

            if (!branchRepository.existsById(branchId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Branch not found with ID: " + branchId
                );
            }
        }

        /*
         * A secretary covering several branches is simply a member of every
         * one of them -- no branch is more "primary" than another for
         * coverage purposes, so branch_staff rows here are never marked
         * is_primary (that flag, and uq_branch_primary_position, exist for
         * BRANCH_LEADER's genuinely-exclusive one-leader-per-branch rule;
         * SECRETARY never participates in it).
         *
         * members.branchId still needs exactly one value (it's a single FK,
         * used for this account's own login/JWT scoping and which member
         * list shows them first) -- keep the existing one if it's still
         * selected, otherwise fall back to the first selected branch.
         */
        Long currentHomeBranch = member.getBranchId();
        Long nextHomeBranch =
                currentHomeBranch != null && desiredBranchIds.contains(currentHomeBranch)
                        ? currentHomeBranch
                        : desiredBranchIds.iterator().next();

        Long actorId = currentUserId();

        if (!nextHomeBranch.equals(currentHomeBranch)) {
            member.setBranchId(nextHomeBranch);
            memberRepository.saveAndFlush(member);
            synchronizeLinkedUserPrimaryBranch(memberId, nextHomeBranch);
        }

        MapSqlParameterSource commonParams =
                new MapSqlParameterSource()
                        .addValue("memberId", memberId)
                        .addValue("appointedBy", actorId);

        // End every currently-active SECRETARY assignment that is no longer selected.
        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    is_primary = FALSE,
                    updated_at = NOW()
                WHERE member_id = :memberId
                  AND position_id = (SELECT id FROM positions WHERE code = 'SECRETARY')
                  AND ended_on IS NULL
                  AND branch_id NOT IN (:branchIds)
                """,
                new MapSqlParameterSource(commonParams.getValues())
                        .addValue("branchIds", desiredBranchIds)
        );

        // Upsert every selected branch. None of these are ever primary.
        for (Long branchId : desiredBranchIds) {
            MapSqlParameterSource params =
                    new MapSqlParameterSource(commonParams.getValues())
                            .addValue("branchId", branchId);

            int updated = jdbcTemplate.update(
                    """
                    UPDATE branch_staff
                    SET ended_on = NULL,
                        is_primary = FALSE,
                        updated_at = NOW(),
                        appointed_by = :appointedBy
                    WHERE branch_id = :branchId
                      AND member_id = :memberId
                      AND position_id = (SELECT id FROM positions WHERE code = 'SECRETARY')
                    """,
                    params
            );

            if (updated == 0) {
                jdbcTemplate.update(
                        """
                        INSERT INTO branch_staff(
                            branch_id, member_id, position_id,
                            started_on, is_primary, appointed_by
                        )
                        VALUES (
                            :branchId, :memberId,
                            (SELECT id FROM positions WHERE code = 'SECRETARY'),
                            CURRENT_DATE, FALSE, :appointedBy
                        )
                        """,
                        params
                );
            }
        }
    }

    @Override
    @Transactional
    public void removeBranch(
            Long memberId,
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        memberAccessValidator
                .validateCanAssignBranch(
                        memberId,
                        branchId
                );

        requireSecretaryAccount(memberId);

        Member member =
                requireMember(memberId);

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "branchId",
                                branchId
                        )
                        .addValue(
                                "memberId",
                                memberId
                        );

        /*
         * The multiselect on the member's personal-info page shows
         * every branch this secretary covers — primary/home branch
         * (members.branch_id) plus any additional branch_staff rows
         * — as one flat, indistinguishable list of checkboxes. There
         * is no separate "primary branch" field for staff to fall
         * back to, so instead of always rejecting removal of the
         * primary branch (the old behavior here), fall back to
         * promoting another one of the secretary's still-active
         * branches to take its place. Only reject when this is
         * genuinely the member's last remaining branch, since every
         * member is required to have one.
         */
        if (branchId.equals(
                member.getBranchId()
        )) {
            Long fallbackBranchId =
                    jdbcTemplate.query(
                            """
                            SELECT branch_id FROM branch_staff
                            WHERE member_id = :memberId
                              AND branch_id <> :branchId
                              AND ended_on IS NULL
                            ORDER BY started_on ASC, id ASC
                            LIMIT 1
                            """,
                            params,
                            resultSet ->
                                    resultSet.next()
                                            ? resultSet.getLong(
                                            "branch_id"
                                    )
                                            : null
                    );

            if (fallbackBranchId == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This is the only branch this secretary covers — assign another branch before removing this one"
                );
            }

            member.setBranchId(
                    fallbackBranchId
            );

            memberRepository
                    .saveAndFlush(member);
            synchronizeLinkedUserPrimaryBranch(memberId, fallbackBranchId);
        }

        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    updated_at = NOW()
                WHERE branch_id = :branchId
                  AND member_id = :memberId
                  AND position_id = (
                      SELECT id FROM positions
                      WHERE code = 'SECRETARY'
                  )
                  AND ended_on IS NULL
                """,
                params
        );
    }

    private void synchronizeLinkedUserPrimaryBranch(
            Long memberId,
            Long branchId
    ) {
        userRepository.findByMemberId(memberId)
                .ifPresent(user -> {
                    if (!java.util.Objects.equals(user.getBranchId(), branchId)) {
                        user.setBranchId(branchId);
                        userRepository.saveAndFlush(user);
                    }
                });
    }

    private User requireSecretaryAccount(
            Long memberId
    ) {
        User targetUser = userRepository
                .findByMemberId(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "This member does not have a user account"
                        )
                );

        if (targetUser.getRole()
                != UserRole.SECRETARY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only secretary accounts can be assigned to additional branches"
            );
        }

        return targetUser;
    }

    private Member requireMember(
            Long memberId
    ) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private Long currentUserId() {
        User principal =
                SecurityUtil.getCurrentUser();

        if (principal == null
                || principal.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return principal.getId();
    }
}
