package org.example.tnal_youth_backend.member.member.mapper;

import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberListResponse toListResponse(Member member) {
        return new MemberListResponse(
                member.getId(),
                member.getMemberNo(),
                member.getFullNameKm(),
                member.getFullNameEn(),
                member.getGender(),
                member.getPhone(),
                member.getEmail(),
                member.getBranchId(),
                toListLookup(member.getStatus()),
                toListLookup(member.getLevel()),
                toListFile(member.getProfilePhoto()),
                member.getJoinedOn()
        );
    }

    public MemberDetailResponse toDetailResponse(Member member) {
        return new MemberDetailResponse(
                member.getId(),
                member.getMemberNo(),
                member.getFullNameKm(),
                member.getFullNameEn(),
                member.getBranchId(),
                toDetailLookup(member.getStatus()),
                toDetailLookup(member.getLevel()),
                toDetailLookup(member.getReligion()),
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

    private MemberListResponse.LookupResponse toListLookup(
            Object lookup
    ) {
        if (lookup == null) {
            return null;
        }

        if (lookup instanceof org.example.tnal_youth_backend.member.status.entity.MemberStatus value) {
            return new MemberListResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof org.example.tnal_youth_backend.member.level.entity.MemberLevel value) {
            return new MemberListResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        return null;
    }

    private MemberDetailResponse.LookupResponse toDetailLookup(
            Object lookup
    ) {
        if (lookup == null) {
            return null;
        }

        if (lookup instanceof org.example.tnal_youth_backend.member.status.entity.MemberStatus value) {
            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof org.example.tnal_youth_backend.member.level.entity.MemberLevel value) {
            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        if (lookup instanceof org.example.tnal_youth_backend.member.religion.entity.Religion value) {
            return new MemberDetailResponse.LookupResponse(
                    value.getId(),
                    value.getCode(),
                    value.getLabelKm(),
                    value.getLabelEn()
            );
        }

        return null;
    }

    private MemberListResponse.FileSummaryResponse toListFile(
            FileEntity file
    ) {
        if (file == null) {
            return null;
        }

        return new MemberListResponse.FileSummaryResponse(
                file.getId(),
                file.getFilePath(),
                file.getOriginalName(),
                file.getMimeType()
        );
    }

    private MemberDetailResponse.FileResponse toDetailFile(
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
}