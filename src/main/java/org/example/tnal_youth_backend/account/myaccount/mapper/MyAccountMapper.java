package org.example.tnal_youth_backend.account.myaccount.mapper;

import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.springframework.stereotype.Component;

@Component
public class MyAccountMapper {

    public MyAccountResponse toResponse(
            User user,
            Member member
    ) {
        if (user == null || member == null) {
            return null;
        }

        MemberStatus memberStatus = member.getStatus();
        MemberLevel memberLevel = member.getLevel();
        Religion religion = member.getReligion();
        FileEntity profilePhoto = member.getProfilePhoto();

        return new MyAccountResponse(

                /*
                 * Account
                 */
                user.getId(),
                member.getId(),
                user.getRole(),
                user.getStatus(),
                member.getPhone(),
                member.getEmail(),
                user.getLastLoginAt(),

                /*
                 * Member profile
                 */
                member.getMemberNo(),
                member.getFullNameKm(),
                member.getFullNameEn(),
                member.getGender(),
                member.getDateOfBirth(),
                member.getPlaceOfBirth(),
                member.getCurrentAddress(),
                member.getPermanentAddress(),
                member.getJoinedOn(),
                member.getBio(),

                /*
                 * Branch
                 */
                member.getBranchId(),

                /*
                 * Member status
                 */
                memberStatus != null
                        ? memberStatus.getId()
                        : null,

                memberStatus != null
                        ? memberStatus.getCode()
                        : null,

                memberStatus != null
                        ? memberStatus.getLabelKm()
                        : null,

                memberStatus != null
                        ? memberStatus.getLabelEn()
                        : null,

                /*
                 * Member level
                 */
                memberLevel != null
                        ? memberLevel.getId()
                        : null,

                memberLevel != null
                        ? memberLevel.getCode()
                        : null,

                memberLevel != null
                        ? memberLevel.getLabelKm()
                        : null,

                memberLevel != null
                        ? memberLevel.getLabelEn()
                        : null,

                /*
                 * Religion
                 */
                religion != null
                        ? religion.getId()
                        : null,

                religion != null
                        ? religion.getCode()
                        : null,

                religion != null
                        ? religion.getLabelKm()
                        : null,

                religion != null
                        ? religion.getLabelEn()
                        : null,

                /*
                 * Profile photo
                 */
                profilePhoto != null
                        ? profilePhoto.getId()
                        : null,

                profilePhoto != null
                        ? profilePhoto.getFilePath()
                        : user.getProfileImage(),

                profilePhoto != null
                        ? profilePhoto.getOriginalName()
                        : null,

                /*
                 * Metadata
                 */
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}