package org.example.tnal_youth_backend.document.document.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentAccessPolicy {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final ActivityRepository activityRepository;

    public User currentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AccessDeniedException("Current user was not found"));
    }

    public boolean canAccess(User user, Document document) {
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        if (user.getRole() == UserRole.MEMBER) {
            return user.getMemberId() != null
                    && user.getMemberId().equals(document.getMemberId());
        }

        Long userBranchId = user.getBranchId();
        if (userBranchId == null) {
            return false;
        }

        Long ownerBranchId = resolveOwnerBranchId(document);
        return userBranchId.equals(ownerBranchId);
    }

    public void requireAccess(User user, Document document) {
        if (!canAccess(user, document)) {
            throw new AccessDeniedException("You cannot access documents outside your branch");
        }
    }

    public void requireOwnerAccess(
            User user,
            Long branchId,
            Long memberId,
            Long activityId
    ) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        Document owner = Document.builder()
                .branchId(branchId)
                .memberId(memberId)
                .activityId(activityId)
                .build();

        requireAccess(user, owner);
    }

    private Long resolveOwnerBranchId(Document document) {
        if (document.getBranchId() != null) {
            return document.getBranchId();
        }

        if (document.getMemberId() != null) {
            return memberRepository.findById(document.getMemberId())
                    .map(member -> member.getBranchId())
                    .orElse(null);
        }

        if (document.getActivityId() != null) {
            return activityRepository.findById(document.getActivityId())
                    .map(activity -> activity.getBranchId())
                    .orElse(null);
        }

        return null;
    }
}
