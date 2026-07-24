package org.example.tnal_youth_backend.account.memberworkhistory.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberworkhistory.service.MyWorkHistoryService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.member.workhistory.entity.MemberWorkHistory;
import org.example.tnal_youth_backend.member.workhistory.mapper.MemberWorkHistoryMapper;
import org.example.tnal_youth_backend.member.workhistory.repository.MemberWorkHistoryRepository;
import org.example.tnal_youth_backend.member.workhistory.service.MemberWorkHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyWorkHistoryServiceImpl
        implements MyWorkHistoryService {

    /*
     * Reuse the Member Management work-history service.
     *
     * It already contains:
     *
     * - member validation
     * - field normalization
     * - date validation
     * - employment-sector foreign-key handling
     * - create, update and delete logic
     */
    private final MemberWorkHistoryService memberWorkHistoryService;

    /*
     * Reuse the existing repository and mapper only for
     * retrieving one owned work-history record.
     */
    private final MemberWorkHistoryRepository memberWorkHistoryRepository;
    private final MemberWorkHistoryMapper memberWorkHistoryMapper;

    /*
     * Used to find the currently authenticated user's
     * linked member profile.
     */
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    /*
     * ==========================================================
     * GET ALL MY WORK HISTORY
     * ==========================================================
     */

    @Override
    public List<MemberWorkHistoryResponse> getMyWorkHistory() {

        Long memberId = getCurrentMemberId();

        return memberWorkHistoryService.getByMemberId(
                memberId
        );
    }

    /*
     * ==========================================================
     * GET ONE MY WORK HISTORY
     * ==========================================================
     */

    @Override
    public MemberWorkHistoryResponse getMyWorkHistoryById(
            Long workId
    ) {
        Long memberId = getCurrentMemberId();

        MemberWorkHistory workHistory =
                findOwnedWorkHistory(
                        memberId,
                        workId
                );

        return memberWorkHistoryMapper.toResponse(
                workHistory
        );
    }

    /*
     * ==========================================================
     * CREATE MY WORK HISTORY
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberWorkHistoryResponse createMyWorkHistory(
            MemberWorkHistoryRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The request does not contain memberId.
         *
         * The member ID is obtained from the authenticated
         * user's users.member_id value.
         */
        return memberWorkHistoryService.create(
                memberId,
                request
        );
    }

    /*
     * ==========================================================
     * UPDATE MY WORK HISTORY
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberWorkHistoryResponse updateMyWorkHistory(
            Long workId,
            MemberWorkHistoryRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The existing service calls:
         *
         * findByIdAndMemberId(workId, memberId)
         *
         * This prevents one member from updating
         * another member's work-history record.
         */
        return memberWorkHistoryService.update(
                memberId,
                workId,
                request
        );
    }

    /*
     * ==========================================================
     * DELETE MY WORK HISTORY
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteMyWorkHistory(
            Long workId
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The existing service performs the ownership check
         * before deleting.
         */
        memberWorkHistoryService.delete(
                memberId,
                workId
        );
    }

    /*
     * ==========================================================
     * FIND OWNED WORK-HISTORY RECORD
     * ==========================================================
     */

    private MemberWorkHistory findOwnedWorkHistory(
            Long memberId,
            Long workId
    ) {
        if (workId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Work history ID is required"
            );
        }

        return memberWorkHistoryRepository
                .findByIdAndMemberId(
                        workId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Work history record was not found"
                        )
                );
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED MEMBER
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
         * Reload the user from the database so that memberId
         * and account information are current.
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

        if (currentUser.getMemberId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not linked to "
                            + "a member profile"
            );
        }

        /*
         * Ensure that users.member_id references an existing
         * members record.
         */
        if (!memberRepository.existsById(
                currentUser.getMemberId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The member profile linked to "
                            + "this account was not found"
            );
        }

        return currentUser.getMemberId();
    }
}