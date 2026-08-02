package org.example.tnal_youth_backend.member.member.mapper;

import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.stereotype.Component;
import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;

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
                toLocalDate(row[17])
        );
    }

    public MemberDetailResponse toDetailResponse(
            Member member
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
                toDetailLookup(member.getStatus()),
                toDetailLookup(member.getLevel()),
                toDetailLookup(member.getReligion()),
                toNationalityResponse(member.getNationality()),
                member.getGender(),
                member.getDateOfBirth(),
                member.getPlaceOfBirth(),
                member.getPhone(),
                member.getEmail(),
                member.getCurrentAddress(),
                member.getPermanentAddress(),
                toDetailFile(member.getProfilePhoto()),
                toDetailFile(member.getCvFile()),
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
                toStringValue(genderCode);

        if (code == null) {
            return null;
        }

        return new MemberListResponse.GenderResponse(
                code,
                toStringValue(genderLabelKm)
        );
    }

    private MemberListResponse.BranchResponse
    toBranchResponse(
            Object branchId,
            Object branchNameKm
    ) {
        Long id =
                toLong(branchId);

        if (id == null) {
            return null;
        }

        return new MemberListResponse.BranchResponse(
                id,
                toStringValue(branchNameKm)
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
                toShort(id);

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
            Object url
    ) {
        Long fileId =
                toLong(id);

        if (fileId == null) {
            return null;
        }

        return new MemberListResponse.ProfilePhotoResponse(
                fileId,
                toStringValue(url)
        );
    }

    private MemberDetailResponse.LookupResponse
    toDetailLookup(
            Object lookup
    ) {
        if (lookup == null) {
            return null;
        }

        if (lookup instanceof
                org.example.tnal_youth_backend.member.status.entity.MemberStatus value) {

            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof
                org.example.tnal_youth_backend.member.level.entity.MemberLevel value) {

            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof
                org.example.tnal_youth_backend.member.religion.entity.Religion value) {

            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        return null;
    }

    private MemberDetailResponse.FileResponse
    toDetailFile(
            FileEntity file
    ) {
        if (file == null) {
            return null;
        }

        return new MemberDetailResponse.FileResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType(),
                file.getSizeBytes()
        );
    }

    private Long toLong(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value).longValue();
    }

    private Short toShort(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        return ((Number) value).shortValue();
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

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof Date date) {
            return date.toLocalDate();
        }

        throw new IllegalArgumentException(
                "Unsupported member date value: "
                        + value.getClass().getName()
        );
    }

    private NationalityResponse toNationalityResponse(
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
}
