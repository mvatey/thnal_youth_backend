package org.example.tnal_youth_backend.document.document.service;

import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentAccessPolicyTest {

    @Mock UserRepository userRepository;
    @Mock MemberRepository memberRepository;
    @Mock ActivityRepository activityRepository;

    @Test
    void adminCanAccessAnyBranchDocument() {
        DocumentAccessPolicy policy = policy();
        User admin = User.builder().role(UserRole.ADMIN).build();
        Document document = Document.builder().branchId(99L).build();

        assertTrue(policy.canAccess(admin, document));
    }

    @Test
    void branchStaffCanAccessMemberDocumentInOwnBranch() {
        DocumentAccessPolicy policy = policy();
        User secretary = User.builder().role(UserRole.SECRETARY).branchId(7L).build();
        Member member = Member.builder().id(20L).branchId(7L).build();
        when(memberRepository.findById(20L)).thenReturn(Optional.of(member));

        assertTrue(policy.canAccess(secretary, Document.builder().memberId(20L).build()));
    }

    @Test
    void branchStaffCannotAccessAnotherBranchDocument() {
        DocumentAccessPolicy policy = policy();
        User leader = User.builder().role(UserRole.BRANCH_LEADER).branchId(7L).build();
        Document document = Document.builder().branchId(8L).build();

        assertFalse(policy.canAccess(leader, document));
        assertThrows(AccessDeniedException.class, () -> policy.requireAccess(leader, document));
    }

    @Test
    void branchStaffWithoutBranchFailsClosed() {
        DocumentAccessPolicy policy = policy();
        User secretary = User.builder().role(UserRole.SECRETARY).build();

        assertFalse(policy.canAccess(secretary, Document.builder().branchId(1L).build()));
    }

    @Test
    void memberCanAccessOnlyTheirOwnMemberDocuments() {
        DocumentAccessPolicy policy = policy();
        User memberUser = User.builder()
                .role(UserRole.MEMBER)
                .memberId(20L)
                .branchId(7L)
                .build();

        assertTrue(policy.canAccess(memberUser, Document.builder().memberId(20L).build()));
        assertFalse(policy.canAccess(memberUser, Document.builder().memberId(21L).build()));
        assertFalse(policy.canAccess(memberUser, Document.builder().branchId(7L).build()));
        assertFalse(policy.canAccess(memberUser, Document.builder().activityId(4L).build()));
    }

    @Test
    void memberWithoutLinkedMemberFailsClosed() {
        DocumentAccessPolicy policy = policy();
        User memberUser = User.builder().role(UserRole.MEMBER).branchId(7L).build();

        assertFalse(policy.canAccess(memberUser, Document.builder().memberId(20L).build()));
    }

    private DocumentAccessPolicy policy() {
        return new DocumentAccessPolicy(userRepository, memberRepository, activityRepository);
    }
}
