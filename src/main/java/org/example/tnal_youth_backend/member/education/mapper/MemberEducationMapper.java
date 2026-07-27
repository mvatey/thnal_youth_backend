package org.example.tnal_youth_backend.member.education.mapper;

import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.education.entity.MemberEducation;
import org.springframework.stereotype.Component;

@Component
public class MemberEducationMapper {

    public MemberEducationResponse toResponse(
            MemberEducation education
    ) {
        if (education == null) {
            return null;
        }

        return new MemberEducationResponse(
                education.getId(),
                education.getMember().getId(),
                education.getSchoolName(),
                education.getEducationLevelId(),
                education.getFieldOfStudy(),
                education.getCountryCode(),
                education.getCountryName(),
                education.getProvinceId(),
                education.getProvinceName(),
                toFileResponse(
                        education.getCertificateFile()
                ),
                education.getStartDate(),
                education.getEndDate(),
                education.getCreatedAt(),
                education.getUpdatedAt()
        );
    }

    private MemberEducationResponse.FileResponse
    toFileResponse(FileEntity file) {
        if (file == null) {
            return null;
        }

        return new MemberEducationResponse.FileResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType(),
                file.getSizeBytes()
        );
    }
}