package org.example.tnal_youth_backend.file.security;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.example.tnal_youth_backend.security.ViewerAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileAccessServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffBranchScopeService staffBranchScopeService;

    @Mock
    private ViewerAccessService viewerAccessService;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void adminCanReadExistingFile() {
        User admin = user(10L, UserRole.ADMIN, null);
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(viewerAccessService.effectiveReadRole(admin)).thenReturn(UserRole.ADMIN);
        when(fileRepository.existsById(50L)).thenReturn(true);

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(10L);

            assertTrue(service().canRead(50L));
        }
    }

    @Test
    void uploaderCanReadUnlinkedFile() {
        User member = user(11L, UserRole.MEMBER, 21L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(member));
        when(fileRepository.existsByIdAndUploadedById(50L, 11L)).thenReturn(true);

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(11L);

            assertTrue(service().canRead(50L));
            // canRead() always probes the organization-branding query first
            // (unstubbed here, so it returns null/false) -- what this test
            // actually cares about is that the heavier member/staff lookup
            // never runs once the uploader check already succeeded.
            verify(jdbcTemplate, never()).queryForObject(
                    argThat(sql -> sql != null && !sql.contains("organization_profile")),
                    any(MapSqlParameterSource.class),
                    eq(Boolean.class)
            );
        }
    }

    @Test
    void memberCanReadFileLinkedToOwnRecord() {
        User member = user(12L, UserRole.MEMBER, 22L);
        when(userRepository.findById(12L)).thenReturn(Optional.of(member));
        when(fileRepository.existsByIdAndUploadedById(50L, 12L)).thenReturn(false);
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && sql.contains("organization_profile")),
                any(MapSqlParameterSource.class),
                eq(Boolean.class)
        )).thenReturn(false);
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && !sql.contains("organization_profile")),
                any(MapSqlParameterSource.class),
                eq(Boolean.class)
        )).thenReturn(true);

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(12L);

            assertTrue(service().canRead(50L));
        }
    }

    @Test
    void branchLeaderCanReadOnlyFileInResolvedScope() {
        User leader = user(13L, UserRole.BRANCH_LEADER, null);
        when(userRepository.findById(13L)).thenReturn(Optional.of(leader));
        when(viewerAccessService.effectiveReadRole(leader)).thenReturn(UserRole.BRANCH_LEADER);
        when(fileRepository.existsByIdAndUploadedById(50L, 13L)).thenReturn(false);
        when(staffBranchScopeService.staffBranchIds(leader)).thenReturn(Set.of(7L));
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && sql.contains("organization_profile")),
                any(MapSqlParameterSource.class),
                eq(Boolean.class)
        )).thenReturn(false);
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && !sql.contains("organization_profile")),
                any(MapSqlParameterSource.class),
                eq(Boolean.class)
        )).thenReturn(true);

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(13L);

            assertTrue(service().canRead(50L));
        }
    }

    @Test
    void unrelatedMemberCannotReadFileByGuessingId() {
        User member = user(14L, UserRole.MEMBER, 24L);
        when(userRepository.findById(14L)).thenReturn(Optional.of(member));
        when(fileRepository.existsByIdAndUploadedById(50L, 14L)).thenReturn(false);
        when(jdbcTemplate.queryForObject(
                any(String.class),
                any(MapSqlParameterSource.class),
                eq(Boolean.class)
        )).thenReturn(false);

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(14L);

            assertFalse(service().canRead(50L));
        }
    }

    @Test
    void unauthenticatedRequestFailsClosed() {
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId)
                    .thenThrow(new IllegalStateException("not authenticated"));

            assertFalse(service().canRead(50L));
            verify(userRepository, never()).findById(any());
        }
    }

    private FileAccessService service() {
        return new FileAccessService(
                fileRepository,
                userRepository,
                staffBranchScopeService,
                viewerAccessService,
                jdbcTemplate
        );
    }

    private User user(Long id, UserRole role, Long memberId) {
        return User.builder()
                .id(id)
                .role(role)
                .memberId(memberId)
                .build();
    }
}
