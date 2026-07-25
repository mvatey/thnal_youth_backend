package org.example.tnal_youth_backend.activity.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.model.response.ActivityParticipantResponse;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityParticipantMapper {

    private final BranchRepository branchRepository;

    public ActivityParticipantResponse toResponse(
            ActivityParticipant participant
    ) {
        if (participant == null) {
            return null;
        }

        Member member = participant.getMember();

        Branch branch = null;

        if (member != null && member.getBranchId() != null) {
            branch = branchRepository
                    .findById(member.getBranchId())
                    .orElse(null);
        }

        return ActivityParticipantResponse.builder()
                .id(participant.getId())
                .activityId(
                        participant.getActivity() != null
                                ? participant.getActivity().getId()
                                : null
                )
                .memberId(
                        member != null
                                ? member.getId()
                                : null
                )
                .memberNo(
                        member != null
                                ? member.getMemberNo()
                                : null
                )
                .fullNameKm(
                        member != null
                                ? member.getFullNameKm()
                                : null
                )
                .fullNameEn(
                        member != null
                                ? member.getFullNameEn()
                                : null
                )
                .phone(
                        member != null
                                ? member.getPhone()
                                : null
                )
                .email(
                        member != null
                                ? member.getEmail()
                                : null
                )
                .branchId(
                        member != null
                                ? member.getBranchId()
                                : null
                )
                .branchCode(
                        branch != null
                                ? branch.getBranchCode()
                                : null
                )
                .branchNameKm(
                        branch != null
                                ? branch.getNameKm()
                                : null
                )
                .branchNameEn(
                        branch != null
                                ? branch.getNameEn()
                                : null
                )
                .attendanceStatusId(
                        participant.getAttendanceStatusId()
                )
                .registrationSource(
                        participant.getRegistrationSource()
                )
                .invitedBranchId(
                        participant.getInvitedBranch() != null
                                ? participant.getInvitedBranch().getId()
                                : null
                )
                .invitedBy(
                        participant.getInvitedBy() != null
                                ? participant.getInvitedBy().getId()
                                : null
                )
                .registeredAt(
                        participant.getRegisteredAt()
                )
                .checkedInAt(
                        participant.getCheckedInAt()
                )
                .checkedOutAt(
                        participant.getCheckedOutAt()
                )
                .note(participant.getNote())
                .build();
    }
}