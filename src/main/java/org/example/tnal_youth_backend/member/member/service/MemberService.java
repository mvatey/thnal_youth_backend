package org.example.tnal_youth_backend.member.member.service;

import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberProfilePhotoRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberStatusRequest;
import org.example.tnal_youth_backend.member.member.dto.response.*;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    MemberPageResponse getMembers(
            int page,
            int size,
            String search,
            Long branchId,
            Short statusId,
            String accountStatus,
            Gender gender
    );

    MemberDetailResponse updateMemberProfilePhoto(
            Long memberId,
            UpdateMemberProfilePhotoRequest request
    );

    MemberDetailResponse uploadMemberProfilePhoto(
            Long memberId,
            MultipartFile file
    );

    MemberSummaryResponse getMemberSummary(Long branchId);

    /** The same cards counted over a set of branches (a branch level pick). */
    MemberSummaryResponse getMemberSummary(java.util.Collection<Long> branchIds);

    /**
     * The cards for the member page's current filters: a branch or set of
     * branches (or everyone the caller may read) narrowed by member status,
     * gender code, position and joined date. Any of the filters may be null.
     */
    MemberSummaryResponse getFilteredMemberSummary(
            Long branchId,
            java.util.Collection<Long> branchIds,
            Long statusId,
            String gender,
            Long positionId,
            java.time.LocalDate joinedOn
    );

    MemberDetailSummaryResponse getMemberDetailSummary(
            Long memberId
    );

    MemberMonthlyDonationSummaryResponse
    getMemberMonthlyDonationSummary(
            Long memberId
    );

    MemberActivityDonationSummaryResponse
    getMemberActivityDonationSummary(
            Long memberId
    );



    MemberDetailResponse getMemberById(
            Long id
    );

    MemberDetailResponse createMember(
            CreateMemberRequest request
    );

    MemberDetailResponse updateMember(
            Long id,
            UpdateMemberRequest request
    );

    MemberDetailResponse updateProfilePhoto(
            Long id,
            MultipartFile file
    );

    void deleteMember(
            Long id
    );

    MemberDetailResponse updateMemberStatus(
            Long id,
            UpdateMemberStatusRequest request
    );
}
