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
                .validateCanManageSensitiveFields(
                        memberId
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
         * A member with no primary branch yet gets this one
         * promoted to primary automatically — otherwise there
         * would be no way to give a brand-new secretary a first
         * branch through this multiselect at all, since removing
         * "the primary branch" is deliberately blocked below.
         */
        boolean becomesPrimary =
                member.getBranchId() == null;

        if (becomesPrimary) {
            member.setBranchId(branchId);

            memberRepository
                    .saveAndFlush(member);
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
                                "isPrimary",
                                becomesPrimary
                        )
                        .addValue(
                                "appointedBy",
                                currentUserId()
                        );

        int reactivated = jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = NULL,
                    is_primary = :isPrimary,
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
                    CURRENT_DATE, :isPrimary, :appointedBy
                )
                """,
                params
        );
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
                .validateCanManageSensitiveFields(
                        memberId
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
        }

        jdbcTemplate.update(
                """
                UPDATE branch_staff
                SET ended_on = CURRENT_DATE,
                    updated_at = NOW()
                WHERE branch_id = :branchId
                  AND member_id = :memberId
                  AND ended_on IS NULL
                """,
                params
        );
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
