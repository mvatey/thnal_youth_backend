package org.example.tnal_youth_backend.member.skill.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberSkillServiceImpl
        implements MemberSkillService {

    private final MemberSkillRepository
            skillRepository;

    private final MemberRepository
            memberRepository;

    private final MemberSkillMapper
            skillMapper;

    private final MemberAccessValidator
            memberAccessValidator;

    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public List<MemberSkillResponse> getByMemberId(
            Long memberId
    ) {
        validateMemberAccess(memberId);

        return skillRepository
                .findAllByMember_IdOrderByIdAsc(
                        memberId
                )
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
        validateMemberAccess(memberId);

        Member member =
                findMember(memberId);

        String skillName =
                normalizeRequired(
                        request.skillName()
                );

        boolean duplicate =
                skillRepository
                        .existsByMember_IdAndSkillNameIgnoreCase(
                                memberId,
                                skillName
                        );

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has the skill: "
                            + skillName
            );
        }

        MemberSkill skill =
                MemberSkill.builder()
                        .member(member)
                        .skillName(skillName)
                        .proficiencyLevelId(
                                request.proficiencyLevelId()
                        )
                        .build();

        try {
            MemberSkill saved =
                    skillRepository
                            .saveAndFlush(
                                    skill
                            );

            return skillMapper.toResponse(
                    saved
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw proficiencyConstraintException();
        }
    }

    @Override
    @Transactional
    public MemberSkillResponse update(
            Long memberId,
            Long skillId,
            MemberSkillRequest request
    ) {
        validateMemberAccess(memberId);

        MemberSkill skill =
                findSkill(
                        memberId,
                        skillId
                );

        String skillName =
                normalizeRequired(
                        request.skillName()
                );

        boolean duplicate =
                skillRepository
                        .existsByMember_IdAndSkillNameIgnoreCaseAndIdNot(
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

        skill.setSkillName(
                skillName
        );

        skill.setProficiencyLevelId(
                request.proficiencyLevelId()
        );

        try {
            MemberSkill updated =
                    skillRepository
                            .saveAndFlush(
                                    skill
                            );

            return skillMapper.toResponse(
                    updated
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw proficiencyConstraintException();
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long skillId
    ) {
        validateMemberAccess(memberId);

        MemberSkill skill =
                findSkill(
                        memberId,
                        skillId
                );

        skillRepository.delete(
                skill
        );
    }

    private void validateMemberAccess(
            Long memberId
    ) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );
    }

    private Member findMember(
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
                .findByIdAndMember_Id(
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

    private String normalizeRequired(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Skill name is required"
            );
        }

        return value.trim();
    }

    private ResponseStatusException
    proficiencyConstraintException() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Skill could not be saved. Check that \
                proficiency_level_id references an existing \
                proficiency level record.
                """
        );
    }

    @Override
    @Transactional
    public MemberSkillResponse uploadCertificate(
            Long memberId,
            Long skillId,
            MultipartFile file
    ) {
        validateMemberAccess(memberId);

        MemberSkill skill =
                findSkill(
                        memberId,
                        skillId
                );

        validateCertificateFile(file);

        FileEntity oldFile =
                skill.getCertificateFile();

        FileEntity uploadedFile =
                fileService.uploadFileEntity(file);

        skill.setCertificateFile(uploadedFile);

        MemberSkill saved =
                skillRepository.saveAndFlush(skill);

        if (oldFile != null) {
            fileService.deleteFile(
                    oldFile.getId()
            );
        }

        return skillMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MemberSkillResponse removeCertificate(
            Long memberId,
            Long skillId
    ) {
        validateMemberAccess(memberId);

        MemberSkill skill =
                findSkill(
                        memberId,
                        skillId
                );

        FileEntity certificateFile =
                skill.getCertificateFile();

        if (certificateFile == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This skill record has no certificate"
            );
        }

        Long fileId =
                certificateFile.getId();

        skill.setCertificateFile(null);

        MemberSkill saved =
                skillRepository.saveAndFlush(skill);

        fileService.deleteFile(fileId);

        return skillMapper.toResponse(saved);
    }

    private void validateCertificateFile(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate file is required"
            );
        }
    }

}