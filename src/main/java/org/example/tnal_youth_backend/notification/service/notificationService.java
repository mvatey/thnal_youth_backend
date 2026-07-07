package org.example.tnal_youth_backend.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.notification.dto.notificationCreateDTO;
import org.example.tnal_youth_backend.notification.dto.notificationDTO;
import org.example.tnal_youth_backend.notification.model.notificationModel;
import org.example.tnal_youth_backend.notification.repo.notificationRepo;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S101") // project-wide lowercase-first module class naming
public class notificationService {

    private final notificationRepo repo;

    /** Creates a notification and fans it out to recipients in one transaction. */
    @Transactional
    public Long create(notificationCreateDTO req) {
        Long actorId = SecurityUtils.getCurrentUserId();

        notificationModel n = notificationModel.builder()
                .typeId(req.getTypeId())
                .title(req.getTitle())
                .body(req.getBody())
                .linkUrl(req.getLinkUrl())
                .programId(req.getProgramId())
                .branchId(req.getBranchId())
                .sentViaInApp(bool(req.getSentViaInApp(), true))
                .sentViaSms(bool(req.getSentViaSms(), false))
                .sentViaEmail(bool(req.getSentViaEmail(), false))
                .createdBy(actorId)
                .build();

        repo.insertNotification(n);
        Long nid = n.getId();
        if (nid == null) {
            throw new BusinessException("NOTIF_INSERT_FAILED", "Failed to persist a notification");
        }

        fanOut(nid, req);

        // Delivery hook: when SMS/email adapters exist, dispatch here based on the sent_via_* flags.
        // Intentionally not enqueuing yet — no provider wired.
        return nid;
    }

    private int fanOut(Long nid, notificationCreateDTO req) {
        return switch (req.getTarget()) {
            case ALL -> repo.fanOutAll(nid);
            case BRANCH -> {
                if (req.getTargetBranchId() == null) {
                    throw new BusinessException("NOTIF_TARGET_BRANCH_REQUIRED",
                            "targetBranchId is required for the BRANCH target");
                }
                yield repo.fanOutBranch(nid, req.getTargetBranchId());
            }
            case PROGRAM_PARTICIPANTS -> {
                if (req.getTargetProgramId() == null) {
                    throw new BusinessException("NOTIF_TARGET_PROGRAM_REQUIRED",
                            "targetProgramId is required for the PROGRAM_PARTICIPANTS target");
                }
                yield repo.fanOutProgramParticipants(nid, req.getTargetProgramId());
            }
            case USERS -> {
                if (req.getTargetUserIds() == null || req.getTargetUserIds().isEmpty()) {
                    throw new BusinessException("NOTIF_TARGET_USERS_REQUIRED",
                            "targetUserIds is required and non-empty for the USERS target");
                }
                yield repo.fanOutUsers(nid, req.getTargetUserIds());
            }
        };
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return repo.countUnread(SecurityUtils.getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public List<notificationDTO> listMine(boolean onlyUnread, int page, int size) {
        int safeSize = Math.clamp(size, 1, 100);
        int safePage = Math.max(page, 0);
        return repo.listForUser(
                SecurityUtils.getCurrentUserId(),
                onlyUnread,
                safeSize,
                safePage * safeSize);
    }

    @Transactional
    public boolean markRead(Long notificationId) {
        return repo.markOneRead(SecurityUtils.getCurrentUserId(), notificationId) > 0;
    }

    @Transactional
    public int markAllRead() {
        return repo.markAllRead(SecurityUtils.getCurrentUserId());
    }

    private static Boolean bool(Boolean v, boolean def) {
        return v == null ? def : v;
    }
}