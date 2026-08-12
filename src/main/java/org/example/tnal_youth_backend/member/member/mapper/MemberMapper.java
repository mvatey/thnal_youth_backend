package org.example.tnal_youth_backend.member.member.mapper;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.ethnicity.dto.response.EthnicityResponse;
import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;

@Component
public class MemberMapper {

    public MemberListResponse toListResponse(
            Object[] row
    ) {
        if (row == null) {
            return null;
        }

        return new MemberListResponse(
                toLong(row[0]),
                toStringValue(row[1]),
                toStringValue(row[2]),

                toGenderResponse(
                        row[3],
                        row[4]
                ),

                toBranchResponse(
                        row[5],
                        row[6]
                ),

                toListLookup(
                        row[7],
                        row[8],
                        row[9],
                        row[10]
                ),

                toListLookup(
                        row[11],
                        row[12],
                        row[13],
                        row[14]
                ),

                toProfilePhotoResponse(
                        row[15],
                        row[16]
                ),

                toLocalDate(
                        row[17]
                )
        );
    }

    public MemberDetailResponse toDetailResponse(
            Member member,
            Branch branch,
            UserRole role
    ) {
        if (member == null) {
            return null;
        }

        return new MemberDetailResponse(
                member.getId(),
                member.getMemberNo(),
                member.getFullNameKm(),
                member.getFullNameEn(),

                member.getBranchId(),

                branch != null
                        ? branch.getBranchCode()
                        : null,

                branch != null
                        ? branch.getNameKm()
                        : null,

                branch != null
                        ? branch.getNameEn()
                        : null,

                role,

                toDetailLookup(
                        member.getStatus()
                ),

                toDetailLookup(
                        member.getLevel()
                ),

                toDetailLookup(
                        member.getReligion()
                ),

                toNationalityResponse(
                        member.getNationality()
                ),

                toEthnicityResponse(
                        member.getEthnicity()
                ),

                member.getGender(),
                member.getDateOfBirth(),
                member.getPlaceOfBirth(),

                member.getTshirtSize() != null
                        ? member.getTshirtSize().getValue()
                        : null,

                member.getPhone(),
                member.getEmail(),
                member.getCurrentAddress(),
                member.getPermanentAddress(),

                toFileResponse(
                        member.getProfilePhoto()
                ),

                toFileResponse(
                        member.getCvFile()
                ),

                member.getJoinedOn(),
                member.getBio(),
                member.getCreatedById(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    private MemberListResponse.GenderResponse
    toGenderResponse(
            Object genderCode,
            Object genderLabelKm
    ) {
        String code =
                toStringValue(
                        genderCode
                );

        if (code == null) {
            return null;
        }

        return new MemberListResponse.GenderResponse(
                code,
                toStringValue(
                        genderLabelKm
                )
        );
    }

    private MemberListResponse.BranchResponse
    toBranchResponse(
            Object branchId,
            Object branchNameKm
    ) {
        Long id =
                toLong(
                        branchId
                );

        if (id == null) {
            return null;
        }

        return new MemberListResponse.BranchResponse(
                id,
                toStringValue(
                        branchNameKm
                )
        );
    }

    private MemberListResponse.LookupResponse
    toListLookup(
            Object id,
            Object code,
            Object labelKm,
            Object labelEn
    ) {
        Short lookupId =
                toShort(
                        id
                );

        if (lookupId == null) {
            return null;
        }

        return new MemberListResponse.LookupResponse(
                lookupId,
                toStringValue(code),
                toStringValue(labelKm),
                toStringValue(labelEn)
        );
    }

    private MemberListResponse.ProfilePhotoResponse
    toProfilePhotoResponse(
            Object id,
            Object filePath
    ) {
        Long fileId =
                toLong(
                        id
                );

        if (fileId == null) {
            return null;
        }

        return new MemberListResponse.ProfilePhotoResponse(
                fileId,
                toStringValue(
                        filePath
                )
        );
    }

    private MemberDetailResponse.LookupResponse
    toDetailLookup(
            Object lookup
    ) {
        if (lookup == null) {
            return null;
        }

        if (
                lookup instanceof
                        org.example.tnal_youth_backend.member.status.entity.MemberStatus value
        ) {
            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (
                lookup instanceof
                        org.example.tnal_youth_backend.member.level.entity.MemberLevel value
        ) {
            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (
                lookup instanceof
                        org.example.tnal_youth_backend.member.religion.entity.Religion value
        ) {
            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        return null;
    }

    private FileResponse toFileResponse(
            FileEntity file
    ) {
        if (file == null) {
            return null;
        }

        Long sizeBytes =
                file.getSizeBytes();

        Double sizeKb =
                sizeBytes == null
                        ? 0.0
                        : sizeBytes / 1024.0;

        Double sizeMb =
                sizeBytes == null
                        ? 0.0
                        : sizeBytes
                        / (1024.0 * 1024.0);

        return new FileResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType(),
                sizeBytes,
                sizeKb,
                sizeMb,
                file.getUploadedById(),
                file.getCreatedAt()
        );
    }

    private NationalityResponse
    toNationalityResponse(
            Nationality nationality
    ) {
        if (nationality == null) {
            return null;
        }

        return new NationalityResponse(
                nationality.getId(),
                nationality.getCode(),
                nationality.getLabelKm(),
                nationality.getLabelEn()
        );
    }

    private EthnicityResponse
    toEthnicityResponse(
            Ethnicity ethnicity
    ) {
        if (ethnicity == null) {
            return null;
        }

        return new EthnicityResponse(
                ethnicity.getId(),
                ethnicity.getCode(),
                ethnicity.getLabelKm(),
                ethnicity.getLabelEn()
        );
    }

    private Long toLong(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value)
                .longValue();
    }

    private Short toShort(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value)
                .shortValue();
    }

    private String toStringValue(
            Object value
    ) {
        return value == null
                ? null
                : value.toString();
    }

    private LocalDate toLocalDate(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        if (
                value instanceof
                        LocalDate localDate
        ) {
            return localDate;
        }

        if (
                value instanceof
                        Date date
        ) {
            return date.toLocalDate();
        }

        throw new IllegalArgumentException(
                "Unsupported member date value: "
                        + value
                        .getClass()
                        .getName()
        );
    }
}