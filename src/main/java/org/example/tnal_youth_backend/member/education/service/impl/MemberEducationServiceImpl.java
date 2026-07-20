package org.example.tnal_youth_backend.member.education.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.education.entity.MemberEducation;
import org.example.tnal_youth_backend.member.education.mapper.MemberEducationMapper;
import org.example.tnal_youth_backend.member.education.repository.MemberEducationRepository;
import org.example.tnal_youth_backend.member.education.service.MemberEducationService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberEducationServiceImpl
        implements MemberEducationService {

    private final MemberEducationRepository educationRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final MemberEducationMapper educationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberEducationResponse> getByMemberId(
            Long memberId
    ) {
        verifyMemberExists(memberId);

        return educationRepository
                .findAllByMemberIdOrderByStartDateDescIdDesc(
                        memberId
                )
                .stream()
                .map(educationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MemberEducationResponse create(
            Long memberId,
            MemberEducationRequest request
    ) {
        Member member = findMember(memberId);

        MemberEducation education =
                MemberEducation.builder()
                        .member(member)
                        .schoolName(
                                normalizeRequired(
                                        request.schoolName(),
                                        "School name"
                                )
                        )
                        .educationLevelId(
                                request.educationLevelId()
                        )
                        .fieldOfStudy(
                                trimToNull(request.fieldOfStudy())
                        )
                        .countryCode(
                                normalizeCountryCode(
                                        request.countryCode()
                                )
                        )
                        .countryName(
                                trimToNull(request.countryName())
                        )
                        .provinceId(request.provinceId())
                        .provinceName(
                                trimToNull(request.provinceName())
                        )
                        .certificateFile(
                                findFile(
                                        request.certificateFileId()
                                )
                        )
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .build();

        try {
            MemberEducation saved =
                    educationRepository
                            .saveAndFlush(education);

            return educationMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException();
        }
    }

    @Override
    @Transactional
    public MemberEducationResponse update(
            Long memberId,
            Long educationId,
            MemberEducationRequest request
    ) {
        MemberEducation education =
                findEducation(memberId, educationId);

        education.setSchoolName(
                normalizeRequired(
                        request.schoolName(),
                        "School name"
                )
        );

        education.setEducationLevelId(
                request.educationLevelId()
        );

        education.setFieldOfStudy(
                trimToNull(request.fieldOfStudy())
        );

        education.setCountryCode(
                normalizeCountryCode(
                        request.countryCode()
                )
        );

        education.setCountryName(
                trimToNull(request.countryName())
        );

        education.setProvinceId(
                request.provinceId()
        );

        education.setProvinceName(
                trimToNull(request.provinceName())
        );

        education.setCertificateFile(
                findFile(request.certificateFileId())
        );

        education.setStartDate(
                request.startDate()
        );

        education.setEndDate(
                request.endDate()
        );

        try {
            MemberEducation updated =
                    educationRepository
                            .saveAndFlush(education);

            return educationMapper.toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException();
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long educationId
    ) {
        MemberEducation education =
                findEducation(memberId, educationId);

        educationRepository.delete(education);
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

    private MemberEducation findEducation(
            Long memberId,
            Long educationId
    ) {
        if (educationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Education ID is required"
            );
        }

        return educationRepository
                .findByIdAndMemberId(
                        educationId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Education record not found with ID: "
                                        + educationId
                                        + " for member ID: "
                                        + memberId
                        )
                );
    }

    private FileEntity findFile(Long fileId) {
        if (fileId == null) {
            return null;
        }

        return fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Certificate file not found with ID: "
                                        + fileId
                        )
                );
    }

    private String normalizeCountryCode(
            String countryCode
    ) {
        String value = trimToNull(countryCode);

        return value == null
                ? null
                : value.toUpperCase(Locale.ROOT);
    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private ResponseStatusException
    databaseConstraintException() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Education record could not be saved. Check that \
                education_level_id, province_id, and \
                certificate_file_id reference existing records.
                """
        );
    }
}