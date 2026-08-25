package org.example.tnal_youth_backend.file.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.example.tnal_youth_backend.security.ViewerAccessService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Object-level read authorization for stored files.
 *
 * <p>A file id is not a capability. Access is granted only to an administrator,
 * the original uploader, the member who owns the linked record, a participant
 * in the linked activity, or staff whose branch scope contains the linked
 * member/document/activity/donation.</p>
 */
@Service("fileAccess")
@RequiredArgsConstructor
@Slf4j
public class FileAccessService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final StaffBranchScopeService staffBranchScopeService;
    private final ViewerAccessService viewerAccessService;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean canRead(Long fileId) {
        if (fileId == null) {
            return false;
        }

        Long userId;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (RuntimeException exception) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() == null) {
            return false;
        }

        /*
         * A VIEWER account's real permission level lives in its
         * viewerScope, not its bare role — same resolution
         * MemberAccessValidator and StaffBranchScopeService already use.
         * Without this, a VIEWER scoped as SECRETARY/BRANCH_LEADER/ADMIN
         * could see a member's/branch's data everywhere else in the app
         * but hit a hard 403 on every file (photo, certificate, receipt...)
         * belonging to that same data.
         */
        UserRole effectiveRole = viewerAccessService.effectiveReadRole(user);

        if (effectiveRole == UserRole.ADMIN) {
            return fileRepository.existsById(fileId);
        }

        if (fileRepository.existsByIdAndUploadedById(fileId, userId)) {
            return true;
        }

        if (user.getMemberId() != null && memberCanRead(fileId, user.getMemberId())) {
            return true;
        }

        if (effectiveRole == UserRole.SECRETARY || effectiveRole == UserRole.BRANCH_LEADER) {
            try {
                return staffCanRead(fileId, staffBranchScopeService.staffBranchIds(user));
            } catch (RuntimeException exception) {
                log.warn("FileAccessService.staffCanRead failed for userId={} fileId={}",
                        userId, fileId, exception);
                return false;
            }
        }

        return false;
    }

    private boolean memberCanRead(Long fileId, Long memberId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM members m
                    WHERE m.id = :memberId
                      AND (m.profile_photo_id = :fileId OR m.cv_file_id = :fileId)
                    UNION ALL
                    SELECT 1 FROM member_education e
                    WHERE e.member_id = :memberId AND e.certificate_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM member_languages l
                    WHERE l.member_id = :memberId AND l.certificate_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM member_skills s
                    WHERE s.member_id = :memberId AND s.certificate_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM member_credentials c
                    WHERE c.member_id = :memberId AND c.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM documents d
                    WHERE d.member_id = :memberId AND d.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM donations d
                    WHERE d.member_id = :memberId AND d.receipt_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM documents d
                    JOIN activity_participants ap ON ap.activity_id = d.activity_id
                    WHERE ap.member_id = :memberId AND d.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activities a
                    JOIN activity_participants ap ON ap.activity_id = a.id
                    WHERE ap.member_id = :memberId AND a.cover_image_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activity_photos p
                    JOIN activity_participants ap ON ap.activity_id = p.activity_id
                    WHERE ap.member_id = :memberId AND p.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activity_attachments a
                    JOIN activity_participants ap ON ap.activity_id = a.activity_id
                    WHERE ap.member_id = :memberId AND a.file_id = :fileId
                )
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("fileId", fileId)
                .addValue("memberId", memberId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }

    private boolean staffCanRead(Long fileId, Set<Long> branchIds) {
        if (branchIds == null || branchIds.isEmpty()) {
            return false;
        }

        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM members m
                    WHERE m.branch_id IN (:branchIds)
                      AND (m.profile_photo_id = :fileId OR m.cv_file_id = :fileId)
                    UNION ALL
                    SELECT 1 FROM member_education e JOIN members m ON m.id = e.member_id
                    WHERE m.branch_id IN (:branchIds) AND e.certificate_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM member_languages l JOIN members m ON m.id = l.member_id
                    WHERE m.branch_id IN (:branchIds) AND l.certificate_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM member_skills s JOIN members m ON m.id = s.member_id
                    WHERE m.branch_id IN (:branchIds) AND s.certificate_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM member_credentials c JOIN members m ON m.id = c.member_id
                    WHERE m.branch_id IN (:branchIds) AND c.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM documents d
                    LEFT JOIN members m ON m.id = d.member_id
                    LEFT JOIN activities a ON a.id = d.activity_id
                    WHERE d.file_id = :fileId
                      AND COALESCE(d.branch_id, m.branch_id, a.branch_id) IN (:branchIds)
                    UNION ALL
                    SELECT 1 FROM donations d
                    WHERE d.branch_id IN (:branchIds) AND d.receipt_file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activities a
                    WHERE a.branch_id IN (:branchIds) AND a.cover_image_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activity_photos p JOIN activities a ON a.id = p.activity_id
                    WHERE a.branch_id IN (:branchIds) AND p.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activity_attachments x JOIN activities a ON a.id = x.activity_id
                    WHERE a.branch_id IN (:branchIds) AND x.file_id = :fileId
                    UNION ALL
                    SELECT 1 FROM activity_expenses e JOIN activities a ON a.id = e.activity_id
                    WHERE a.branch_id IN (:branchIds) AND e.receipt_file_id = :fileId
                )
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("fileId", fileId)
                .addValue("branchIds", branchIds);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, parameters, Boolean.class));
    }
}
