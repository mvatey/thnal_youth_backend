package org.example.tnal_youth_backend.member.education.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.education.entity.MemberEducation;
import org.example.tnal_youth_backend.member.education.mapper.MemberEducationMapper;
import org.example.tnal_youth_backend.member.education.repository.MemberEducationRepository;
import org.example.tnal_youth_backend.member.education.service.MemberEducationService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MemberEducationServiceImpl
        implements MemberEducationService {

    private static final long MAX_CERTIFICATE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String>
            ALLOWED_CERTIFICATE_EXTENSIONS =
            Set.of(
                    "pdf",
                    "doc",
                    "docx",
                    "jpg",
                    "jpeg",
                    "png"
            );

    private final MemberEducationRepository educationRepository;
    private final MemberRepository memberRepository;
    private final MemberEducationMapper educationMapper;
    private final FileService fileService;
    private final FileRepository fileRepository;

    private final MemberAccessValidator
            memberAccessValidator;

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
                        .countryName(
                                normalizeRequired(
                                        request.countryName(),
                                        "Country name"
                                )
                        )
                        .provinceName(
                                trimToNull(
                                        request.provinceName()
                                )
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

        education.setCountryName(
                normalizeRequired(
                        request.countryName(),
                        "Country name"
                )
        );

        education.setProvinceName(
                trimToNull(
                        request.provinceName()
                )
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

    @Override
    @Transactional
    public MemberEducationResponse uploadCertificate(
            Long memberId,
            Long educationId,
            MultipartFile file
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        MemberEducation education =
                findEducation(
                        memberId,
                        educationId
                );

        validateCertificateFile(
                file
        );

        FileEntity uploadedFile =
                fileService.uploadFileEntity(
                        file
                );

        education.setCertificateFile(
                uploadedFile
        );

        MemberEducation savedEducation =
                educationRepository
                        .saveAndFlush(
                                education
                        );

        return educationMapper.toResponse(
                savedEducation
        );
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

        if (file.getSize() > MAX_CERTIFICATE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate file must not exceed 10 MB"
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (
                originalFilename == null
                        || originalFilename.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate file name is required"
            );
        }

        String extension =
                getFileExtension(
                        originalFilename
                );

        if (
                !ALLOWED_CERTIFICATE_EXTENSIONS
                        .contains(extension)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, DOC, DOCX, JPG, JPEG, and PNG files are allowed"
            );
        }
    }

    private String getFileExtension(
            String filename
    ) {
        int lastDot =
                filename.lastIndexOf('.');

        if (
                lastDot < 0
                        || lastDot
                        == filename.length() - 1
        ) {
            return "";
        }

        return filename
                .substring(
                        lastDot + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }

}