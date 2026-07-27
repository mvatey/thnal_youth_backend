package org.example.tnal_youth_backend.member.skill.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.skill.entity.MemberSkill;
import org.example.tnal_youth_backend.member.skill.mapper.MemberSkillMapper;
import org.example.tnal_youth_backend.member.skill.repository.MemberSkillRepository;
import org.example.tnal_youth_backend.member.skill.service.MemberSkillService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberSkillServiceImpl
        implements MemberSkillService {

    private final MemberSkillRepository skillRepository;
    private final MemberRepository memberRepository;
    private final MemberSkillMapper skillMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberSkillResponse> getByMemberId(
            Long memberId
    ) {
        verifyMemberExists(memberId);

        return skillRepository
                .findAllByMemberIdOrderByIdAsc(memberId)
                .stream()
                .map(skillMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MemberSkillResponse create(
            Long memberId,
            MemberSkillRequest request
    ) {
        Member member = findMember(memberId);

        String skillName =
                normalizeRequired(request.skillName());

        if (skillRepository
                .existsByMemberIdAndSkillNameIgnoreCase(
                        memberId,
                        skillName
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has the skill: "
                            + skillName
            );
        }

        MemberSkill skill = MemberSkill.builder()
                .member(member)
                .skillName(skillName)
                .proficiencyLevelId(
                        request.proficiencyLevelId()
                )
                .build();

        try {
            MemberSkill saved =
                    skillRepository.saveAndFlush(skill);

            return skillMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Skill could not be saved. Check that \
                    proficiency_level_id references an existing record.
                    """
            );
        }
    }

    @Override
    @Transactional
    public MemberSkillResponse update(
            Long memberId,
            Long skillId,
            MemberSkillRequest request
    ) {
        MemberSkill skill =
                findSkill(memberId, skillId);

        String skillName =
                normalizeRequired(request.skillName());

        boolean duplicate =
                skillRepository
                        .existsByMemberIdAndSkillNameIgnoreCaseAndIdNot(
                                memberId,
                                skillName,
                                skillId
                        );

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has the skill: "
                            + skillName
            );
        }

        skill.setSkillName(skillName);
        skill.setProficiencyLevelId(
                request.proficiencyLevelId()
        );

        try {
            MemberSkill updated =
                    skillRepository.saveAndFlush(skill);

            return skillMapper.toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Skill could not be updated. Check that \
                    proficiency_level_id references an existing record.
                    """
            );
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long skillId
    ) {
        MemberSkill skill =
                findSkill(memberId, skillId);

        skillRepository.delete(skill);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private void verifyMemberExists(Long memberId) {
        findMember(memberId);
    }

    private MemberSkill findSkill(
            Long memberId,
            Long skillId
    ) {
        if (skillId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Skill ID is required"
            );
        }

        return skillRepository
                .findByIdAndMemberId(
                        skillId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Skill not found with ID: "
                                        + skillId
                                        + " for member ID: "
                                        + memberId
                        )
                );
    }

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Skill name is required"
            );
        }

        return value.trim();
    }
}