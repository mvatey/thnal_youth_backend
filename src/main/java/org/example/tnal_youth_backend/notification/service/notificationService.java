package org.example.tnal_youth_backend.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.notification.dto.notificationCreateDTO;
import org.example.tnal_youth_backend.notification.dto.notificationCreateResultDTO;
import org.example.tnal_youth_backend.notification.dto.notificationDTO;
import org.example.tnal_youth_backend.notification.dto.notificationPageDTO;
import org.example.tnal_youth_backend.notification.model.notificationModel;
import org.example.tnal_youth_backend.notification.repo.notificationRepo;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class notificationService {

    private final notificationRepo repo;

    /**
     * Creates a notification and fans it out to recipients in one transaction.
     *
     * SMS / email delivery is NOT dispatched inline — blocking the DB transaction
     * on a network call is wrong, and rollbacks would fire already-sent messages.
     * When SMS/email adapters land, publish a Spring event here and consume it in
     * a @TransactionalEventListener(phase = AFTER_COMMIT).
     *
     * Sender attribution lives on notifications.created_by, populated from the
     * authenticated principal. The notifications table is not in the DB-side
     * audit_trigger loop, but the row is immutable via the API (no update/delete
     * endpoint), so created_by is a sufficient trail.
     */
    @Transactional
    public notificationCreateResultDTO create(notificationCreateDTO req) {
        // ---- validate at least one delivery channel is enabled ----
        Boolean inApp = bool(req.getSentViaInApp(), true);
        Boolean sms   = bool(req.getSentViaSms(),   false);
        Boolean email = bool(req.getSentViaEmail(), false);

        if (!inApp && !sms && !email) {
            throw new BusinessException("NOTIF_NO_CHANNEL",
                    "At least one delivery channel (in-app, SMS, or email) must be enabled");
        }

        // ---- validate notification type is active ----
        if (repo.countActiveType(req.getTypeId()) == 0) {
            throw new BusinessException("NOTIF_TYPE_INACTIVE",
                    "Notification type " + req.getTypeId() + " does not exist or is inactive");
        }

        Long actorId = SecurityUtils.getCurrentUserId();

        notificationModel n = notificationModel.builder()
                .typeId(req.getTypeId())
                .title(req.getTitle())
                .body(req.getBody())
                .linkUrl(req.getLinkUrl())
                .programId(req.getProgramId())
                .branchId(req.getBranchId())
                .sentViaInApp(inApp)
                .sentViaSms(sms)
                .sentViaEmail(email)
                .createdBy(actorId)
                .build();

        repo.insertNotification(n);
        Long nid = n.getId();
        if (nid == null) {
            throw new BusinessException("NOTIF_INSERT_FAILED", "Failed to persist a notification");
        }

        int recipientCount = fanOut(nid, req);
        // Zero-audience is surfaced to the caller (not thrown) so admins can see
        // the effect of their target choice without a hard failure on
        // legitimately-empty branches or programs.
        return new notificationCreateResultDTO(nid, recipientCount, n.getCreatedAt());
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
    public notificationPageDTO listMine(boolean onlyUnread, int page, int size) {
        int safeSize = Math.clamp(size, 1, 100);
        int safePage = Math.max(page, 0);
        Long userId = SecurityUtils.getCurrentUserId();

        List<notificationDTO> items = repo.listForUser(userId, onlyUnread, safeSize, safePage * safeSize);
        long total = repo.countForUser(userId, onlyUnread);

        return new notificationPageDTO(items, total, safePage, safeSize);
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