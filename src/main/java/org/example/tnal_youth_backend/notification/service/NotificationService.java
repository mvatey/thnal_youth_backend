package org.example.tnal_youth_backend.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateResultDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationDTO;
import org.example.tnal_youth_backend.notification.dto.NotificationPageDTO;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.example.tnal_youth_backend.notification.repo.NotificationRepo;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo repo;

    /**
     * Creates a notification and fans it out to recipients in one transaction.
     *
     * <p>The current schema (V11) models notifications as IN-APP ONLY: a notification
     * is a row in `notifications` plus one row per recipient in
     * `notification_recipients`. There are no per-channel flags. If SMS/email
     * delivery is added later it needs (a) schema columns/tables and (b) an
     * out-of-transaction dispatch — publish a Spring event here and consume it in
     * a {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so a rollback
     * never fires an already-sent message.
     *
     * <p>Sender attribution lives on notifications.created_by, populated from the
     * authenticated principal. The row is immutable via the API (no update/delete
     * endpoint), so created_by is a sufficient trail.
     *
     * <p><b>Idempotency (V21):</b> if the caller supplies {@code clientRequestId},
     * a repeated create from the same actor returns the original notification
     * instead of fanning out again. Sequential retries (the common double-submit
     * case) are collapsed by the pre-check below. Truly concurrent duplicates are
     * stopped by the partial unique index uq_notifications_creator_client_request:
     * the losing request receives a DATA_INTEGRITY_VIOLATION (400) and no second
     * notification is ever created. Omitting the key preserves the previous,
     * non-idempotent behaviour.
     */
    @Transactional
    public NotificationCreateResultDTO create(NotificationCreateDTO req) {
        // ---- validate notification type is active ----
        if (repo.countActiveType(req.getTypeId()) == 0) {
            throw new BusinessException("NOTIF_TYPE_INACTIVE",
                    "Notification type " + req.getTypeId() + " does not exist or is inactive");
        }

        Long actorId = SecurityUtils.getCurrentUserId();
        String clientRequestId = normalizeToNull(req.getClientRequestId());

        // ---- idempotency short-circuit (already-committed replay) ----
        if (clientRequestId != null) {
            Long existing = repo.findIdByCreatorAndClientRequestId(actorId, clientRequestId);
            if (existing != null) {
                return buildResult(existing);
            }
        }

        NotificationModel n = NotificationModel.builder()
                .typeId(req.getTypeId())
                .title(req.getTitle())
                .body(req.getBody())
                // Normalise blank to NULL so we never violate chk_notification_action_url
                // (action_url IS NULL OR BTRIM(action_url) <> '').
                .actionUrl(normalizeToNull(req.getActionUrl()))
                .activityId(req.getActivityId())
                .branchId(req.getBranchId())
                .createdBy(actorId)
                .clientRequestId(clientRequestId)
                .build();

        repo.insertNotification(n);
        Long nid = n.getId();
        if (nid == null) {
            throw new BusinessException("NOTIF_INSERT_FAILED", "Failed to persist a notification");
        }

        int recipientCount = fanOut(nid, req);
        // Zero-audience is surfaced to the caller (not thrown) so admins can see
        // the effect of their target choice without a hard failure on
        // legitimately-empty branches or activities.
        return new NotificationCreateResultDTO(nid, recipientCount, n.getCreatedAt());
    }

    private int fanOut(Long nid, NotificationCreateDTO req) {
        return switch (req.getTarget()) {
            case ALL -> repo.fanOutAll(nid);
            case BRANCH -> {
                if (req.getTargetBranchId() == null) {
                    throw new BusinessException("NOTIF_TARGET_BRANCH_REQUIRED",
                            "targetBranchId is required for the BRANCH target");
                }
                yield repo.fanOutBranch(nid, req.getTargetBranchId());
            }
            case ACTIVITY_PARTICIPANTS -> {
                if (req.getTargetActivityId() == null) {
                    throw new BusinessException("NOTIF_TARGET_ACTIVITY_REQUIRED",
                            "targetActivityId is required for the ACTIVITY_PARTICIPANTS target");
                }
                yield repo.fanOutActivityParticipants(nid, req.getTargetActivityId());
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

    /** Rebuilds a create-result for an idempotent replay of an existing notification. */
    private NotificationCreateResultDTO buildResult(Long nid) {
        OffsetDateTime createdAt = repo.findCreatedAtById(nid);
        int recipientCount = repo.countRecipients(nid);
        return new NotificationCreateResultDTO(nid, recipientCount, createdAt);
    }

    private static String normalizeToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return repo.countUnread(SecurityUtils.getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public NotificationPageDTO listMine(boolean onlyUnread, int page, int size) {
        int safeSize = Math.clamp(size, 1, 100);
        int safePage = Math.max(page, 0);
        Long userId = SecurityUtils.getCurrentUserId();

        List<NotificationDTO> items = repo.listForUser(userId, onlyUnread, safeSize, safePage * safeSize);
        long total = repo.countForUser(userId, onlyUnread);

        return new NotificationPageDTO(items, total, safePage, safeSize);
    }

    @Transactional
    public boolean markRead(Long notificationId) {
        return repo.markOneRead(SecurityUtils.getCurrentUserId(), notificationId) > 0;
    }

    @Transactional
    public int markAllRead() {
        return repo.markAllRead(SecurityUtils.getCurrentUserId());
    }
}
