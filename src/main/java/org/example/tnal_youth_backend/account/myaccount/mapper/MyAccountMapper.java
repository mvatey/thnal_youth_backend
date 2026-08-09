package org.example.tnal_youth_backend.account.myaccount.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyAccountMapper {

    private final JdbcTemplate jdbcTemplate;

    public MyAccountResponse toResponse(
            User user,
            Member member
    ) {
        if (user == null || member == null) {
            return null;
        }

        return new MyAccountResponse(
                user.getId(),
                member.getId(),
                member.getMemberNo(),
                user.getRole(),
                member.getFullNameKm(),
                member.getFullNameEn(),
                toGenderResponse(
                        member.getGender()
                ),
                toBranchResponse(
                        member.getBranchId()
                ),
                toLookupResponse(
                        member.getLevel()
                ),
                toLookupResponse(
                        member.getStatus()
                ),
                toLookupResponse(member.getReligion()),
                toLookupResponse(member.getNationality()),
                toLookupResponse(member.getEthnicity()),
                member.getPhone(),
                member.getEmail(),
                member.getDateOfBirth(),
                member.getJoinedOn(),
                member.getPlaceOfBirth(),
                member.getCurrentAddress(),
                member.getPermanentAddress(),
                member.getTshirtSize(),
                member.getBio(),
                toProfilePhotoResponse(
                        member.getProfilePhoto(),
                        user.getProfileImage()
                ),
                toCvFileResponse(
                        member.getCvFile()
                )
        );
    }

    private MyAccountResponse.GenderResponse
    toGenderResponse(
            Gender gender
    ) {
        if (gender == null) {
            return null;
        }

        return switch (gender) {
            case MALE ->
                    new MyAccountResponse.GenderResponse(
                            "MALE",
                            "ប្រុស",
                            "Male"
                    );

            case FEMALE ->
                    new MyAccountResponse.GenderResponse(
                            "FEMALE",
                            "ស្រី",
                            "Female"
                    );

            case MONK ->
                    new MyAccountResponse.GenderResponse(
                            "OTHER",
                            "ព្រះសង្ឃ",
                            "Monk"
                    );
        };
    }

    private MyAccountResponse.BranchResponse
    toBranchResponse(
            Long branchId
    ) {
        if (branchId == null) {
            return null;
        }

        List<MyAccountResponse.BranchResponse> results =
                jdbcTemplate.query(
                        """
                        SELECT
                            id,
                            name_km
                        FROM branches
                        WHERE id = ?
                        """,
                        (
                                resultSet,
                                rowNumber
                        ) -> new MyAccountResponse.BranchResponse(
                                resultSet.getLong("id"),
                                resultSet.getString("name_km")
                        ),
                        branchId
                );

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    private MyAccountResponse.LookupResponse
    toLookupResponse(
            Object lookup
    ) {
        if (lookup == null) {
            return null;
        }

        if (lookup instanceof MemberLevel value) {
            return new MyAccountResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof MemberStatus value) {
            return new MyAccountResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof Religion value) {
            return new MyAccountResponse.LookupResponse(value.getId(), value.getCode(), value.getLabelKm(), value.getLabelEn());
        }

        if (lookup instanceof Nationality value) {
            return new MyAccountResponse.LookupResponse(value.getId(), value.getCode(), value.getLabelKm(), value.getLabelEn());
        }

        if (lookup instanceof Ethnicity value) {
            return new MyAccountResponse.LookupResponse(value.getId(), value.getCode(), value.getLabelKm(), value.getLabelEn());
        }

        return null;
    }

    private MyAccountResponse.ProfilePhotoResponse
    toProfilePhotoResponse(
            FileEntity profilePhoto,
            String fallbackProfileImage
    ) {
        if (profilePhoto != null) {
            return new MyAccountResponse.ProfilePhotoResponse(
                    profilePhoto.getId(),
                    profilePhoto.getFilePath()
            );
        }

        if (fallbackProfileImage == null
                || fallbackProfileImage.isBlank()) {

            return null;
        }

        return new MyAccountResponse.ProfilePhotoResponse(
                null,
                fallbackProfileImage
        );
    }

    private MyAccountResponse.CvFileResponse
    toCvFileResponse(
            FileEntity cvFile
    ) {
        if (cvFile == null) {
            return null;
        }

        return new MyAccountResponse.CvFileResponse(
                cvFile.getId(),
                cvFile.getFilePath(),
                cvFile.getOriginalName(),
                cvFile.getMimeType(),
                cvFile.getSizeBytes()
        );
    }
}
