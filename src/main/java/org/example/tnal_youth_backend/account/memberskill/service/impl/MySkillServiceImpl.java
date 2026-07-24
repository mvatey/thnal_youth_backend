package org.example.tnal_youth_backend.account.memberskill.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.memberskill.service.MySkillService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.skill.entity.MemberSkill;
import org.example.tnal_youth_backend.member.skill.mapper.MemberSkillMapper;
import org.example.tnal_youth_backend.member.skill.repository.MemberSkillRepository;
import org.example.tnal_youth_backend.member.skill.service.MemberSkillService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MySkillServiceImpl
        implements MySkillService {

    /*
     * Reuse the existing Member Skill service.
     *
     * It already handles:
     *
     * - member validation
     * - skill-name normalization
     * - duplicate skill validation
     * - proficiency-level foreign-key validation
     * - ownership checks for update and delete
     */
    private final MemberSkillService memberSkillService;

    /*
     * Used only to retrieve one skill that belongs
     * to the currently logged-in member.
     */
    private final MemberSkillRepository memberSkillRepository;
    private final MemberSkillMapper memberSkillMapper;

    /*
     * Used to resolve users.member_id.
     */
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    /*
     * ==========================================================
     * GET ALL MY SKILLS
     * ==========================================================
     */

    @Override
    public List<MemberSkillResponse> getMySkills() {

        Long memberId = getCurrentMemberId();

        return memberSkillService.getByMemberId(
                memberId
        );
    }

    /*
     * ==========================================================
     * GET ONE MY SKILL
     * ==========================================================
     */

    @Override
    public MemberSkillResponse getMySkillById(
            Long skillId
    ) {
        Long memberId = getCurrentMemberId();

        MemberSkill skill =
                findOwnedSkill(
                        memberId,
                        skillId
                );

        return memberSkillMapper.toResponse(
                skill
        );
    }

    /*
     * ==========================================================
     * CREATE MY SKILL
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberSkillResponse createMySkill(
            MemberSkillRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The frontend does not send memberId.
         *
         * The backend obtains it from the logged-in
         * user's users.member_id value.
         */
        return memberSkillService.create(
                memberId,
                request
        );
    }

    /*
     * ==========================================================
     * UPDATE MY SKILL
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberSkillResponse updateMySkill(
            Long skillId,
            MemberSkillRequest request
    ) {
        Long memberId = getCurrentMemberId();

        /*
         * The existing service checks ownership using:
         *
         * findByIdAndMemberId(skillId, memberId)
         */
        return memberSkillService.update(
                memberId,
                skillId,
                request
        );
    }

    /*
     * ==========================================================
     * DELETE MY SKILL
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteMySkill(
            Long skillId
    ) {
        Long memberId = getCurrentMemberId();

        memberSkillService.delete(
                memberId,
                skillId
        );
    }

    /*
     * ==========================================================
     * FIND OWNED SKILL
     * ==========================================================
     */

    private MemberSkill findOwnedSkill(
            Long memberId,
            Long skillId
    ) {
        if (skillId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Skill ID is required"
            );
        }

        return memberSkillRepository
                .findByIdAndMemberId(
                        skillId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Skill record was not found"
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
         * Reload the user from the database so memberId is current.
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