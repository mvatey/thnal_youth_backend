package org.example.tnal_youth_backend.notification.repo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.tnal_youth_backend.notification.dto.NotificationDTO;
import org.example.tnal_youth_backend.notification.model.NotificationModel;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * All SQL is written against the committed schema (V11 notifications /
 * notification_recipients, V6 activity_participants, V7 member_statuses,
 * V14 users.status, V22 notifications.client_request_id). Columns are aliased
 * explicitly because map-underscore-to-camel-case is NOT configured for MyBatis
 * in this project.
 */
@Mapper
public interface NotificationRepo {

    // ---------- lookup validation ----------

    @Select("""
        SELECT COUNT(*)
        FROM notification_types
        WHERE id = #{typeId} AND is_active = TRUE
        """)
    int countActiveType(@Param("typeId") Short typeId);

    /**
     * Resolves a notification_types.code (e.g. "ACTIVITY_INVITATION",
     * "ACTIVITY_REMINDER") to its active id, or {@code null} if the code does
     * not exist or is inactive. Used by callers that only know the type by its
     * stable code rather than its numeric id.
     */
    @Select("""
        SELECT id
        FROM notification_types
        WHERE code = #{code} AND is_active = TRUE
        """)
    Short findActiveTypeIdByCode(@Param("code") String code);

    // ---------- idempotency ----------

    /**
     * Returns the id of a prior notification created by the same actor with the
     * same idempotency key, or {@code null}. Backed by the partial unique index
     * uq_notifications_creator_client_request (V22).
     */
    @Select("""
        SELECT id
        FROM notifications
        WHERE created_by = #{createdBy}
          AND client_request_id = #{clientRequestId}::uuid
        """)
    Long findIdByCreatorAndClientRequestId(@Param("createdBy") Long createdBy,
                                           @Param("clientRequestId") String clientRequestId);

    @Select("""
        SELECT created_at
        FROM notifications
        WHERE id = #{nid}
        """)
    OffsetDateTime findCreatedAtById(@Param("nid") Long notificationId);

    /**
     * Full row lookup used by {@code NotificationDispatchListener} to read
     * back a just-committed notification's title/body/action_url for
     * email/Telegram dispatch. Returns {@code null} if the id doesn't
     * exist (defensive — should not happen for a notification id that was
     * just inserted in the same, now-committed, transaction).
     */
    @Select("""
        SELECT
            id             AS id,
            type_id        AS typeId,
            title          AS title,
            body           AS body,
            action_url     AS actionUrl,
            activity_id    AS activityId,
            branch_id      AS branchId,
            created_by     AS createdBy,
            client_request_id AS clientRequestId,
            created_at     AS createdAt
        FROM notifications
        WHERE id = #{nid}
        """)
    NotificationModel findById(@Param("nid") Long notificationId);

    /**
     * Every recipient's user id for a notification, used by
     * {@code NotificationDispatchListener} to drive the email/Telegram
     * fan-out after the in-app fan-out has already committed.
     */
    @Select("""
        SELECT user_id
        FROM notification_recipients
        WHERE notification_id = #{nid}
        """)
    List<Long> findRecipientUserIds(@Param("nid") Long notificationId);

    // ---------- per-channel delivery tracking (V332 notification_deliveries) ----------
    // Observability only — a failed/skipped row here never blocks anything, it just
    // records what NotificationDispatchListener actually attempted for each recipient.

    @Insert("""
        INSERT INTO notification_deliveries
            (notification_id, user_id, channel, status, error_message)
        VALUES
            (#{notificationId}, #{userId}, #{channel}, #{status}, #{errorMessage})
        ON CONFLICT (notification_id, user_id, channel)
        DO UPDATE SET
            status = EXCLUDED.status,
            error_message = EXCLUDED.error_message,
            created_at = NOW()
        """)
    int insertDelivery(@Param("notificationId") Long notificationId,
                       @Param("userId") Long userId,
                       @Param("channel") String channel,
                       @Param("status") String status,
                       @Param("errorMessage") String errorMessage);

    @Select("""
        SELECT COUNT(*)
        FROM notification_recipients
        WHERE notification_id = #{nid}
        """)
    int countRecipients(@Param("nid") Long notificationId);

    // ---------- notifications ----------

    @Insert("""
        INSERT INTO notifications
            (type_id, title, body, action_url, activity_id, branch_id, created_by, client_request_id)
        VALUES
            (#{typeId}, #{title}, #{body}, #{actionUrl}, #{activityId}, #{branchId}, #{createdBy}, #{clientRequestId}::uuid)
        """)
    @Options(useGeneratedKeys = true,
            keyProperty = "id,createdAt",
            keyColumn = "id,created_at")
    int insertNotification(NotificationModel n);

    // ---------- recipient fan-out ----------
    // notification_recipients.is_read defaults to FALSE and read_at to NULL,
    // which satisfies chk_notification_recipient_read_state, so inserts only
    // supply (notification_id, user_id).

    @Insert("""
        INSERT INTO notification_recipients (notification_id, user_id)
        SELECT #{nid}, u.id
        FROM users u
        WHERE u.status = 'ACTIVE'
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """)
    int fanOutAll(@Param("nid") Long notificationId);

    // members has no user_id; the account link is users.member_id = members.id.
    @Insert("""
        INSERT INTO notification_recipients (notification_id, user_id)
        SELECT #{nid}, u.id
        FROM users u
        JOIN members m          ON m.id = u.member_id
        JOIN member_statuses ms ON ms.id = m.status_id
        WHERE m.branch_id = #{branchId}
          AND u.status = 'ACTIVE'
          AND ms.code <> 'INACTIVE'
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """)
    int fanOutBranch(@Param("nid") Long notificationId,
                     @Param("branchId") Long branchId);

    @Insert("""
        INSERT INTO notification_recipients (notification_id, user_id)
        SELECT DISTINCT #{nid}, u.id
        FROM activity_participants ap
        JOIN members m ON m.id = ap.member_id
        JOIN users   u ON u.member_id = m.id
        WHERE ap.activity_id = #{activityId}
          AND u.status = 'ACTIVE'
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """)
    int fanOutActivityParticipants(@Param("nid") Long notificationId,
                                   @Param("activityId") Long activityId);

    @Insert({
            "<script>",
            "INSERT INTO notification_recipients (notification_id, user_id)",
            "SELECT #{nid}, u.id",
            "FROM users u",
            "WHERE u.status = 'ACTIVE'",
            "  AND u.id IN",
            "<foreach collection='userIds' item='uid' open='(' separator=',' close=')'>",
            "  #{uid}",
            "</foreach>",
            "ON CONFLICT (notification_id, user_id) DO NOTHING",
            "</script>"
    })
    int fanOutUsers(@Param("nid") Long notificationId,
                    @Param("userIds") List<Long> userIds);

    // ---------- inbox reads ----------
    // is_read is the canonical unread flag; the partial index
    // idx_notification_recipients_unread (WHERE is_read = FALSE) backs these.

    /**
     * branchId is optional (see NotificationService.listMine/unreadCount): a
     * notification with no branch_id (a purely personal one — "your document
     * was issued", account activation, etc.) always counts/shows regardless
     * of which branch is currently selected in the sidebar. A notification
     * WITH a branch_id (an activity invite sent to a specific branch, etc.)
     * only counts/shows while that same branch is selected. Passing NULL for
     * branchId disables this filter entirely (the "all branches" view).
     */
    @Select("""
        SELECT COUNT(*)
        FROM notification_recipients nr
        JOIN notifications n ON n.id = nr.notification_id
        WHERE nr.user_id = #{userId}
          AND nr.is_read = FALSE
          AND (#{branchId}::bigint IS NULL OR n.branch_id IS NULL OR n.branch_id = #{branchId}::bigint)
        """)
    long countUnread(@Param("userId") Long userId,
                     @Param("branchId") Long branchId);

    @Select("""
        SELECT COUNT(*)
        FROM notification_recipients nr
        JOIN notifications n ON n.id = nr.notification_id
        WHERE nr.user_id = #{userId}
          AND (#{onlyUnread}::boolean = FALSE OR nr.is_read = FALSE)
          AND (#{branchId}::bigint IS NULL OR n.branch_id IS NULL OR n.branch_id = #{branchId}::bigint)
        """)
    long countForUser(@Param("userId") Long userId,
                      @Param("onlyUnread") boolean onlyUnread,
                      @Param("branchId") Long branchId);

    @Select("""
        SELECT
            n.id            AS id,
            nt.code         AS typeCode,
            nt.label_km     AS typeLabelKm,
            nt.label_en     AS typeLabelEn,
            n.title         AS title,
            n.body          AS body,
            n.action_url    AS actionUrl,
            n.activity_id   AS activityId,
            n.branch_id     AS branchId,
            n.created_at    AS createdAt,
            nr.is_read      AS isRead,
            nr.read_at      AS readAt
        FROM notification_recipients nr
        JOIN notifications      n  ON n.id = nr.notification_id
        JOIN notification_types nt ON nt.id = n.type_id
        WHERE nr.user_id = #{userId}
          AND (#{onlyUnread}::boolean = FALSE OR nr.is_read = FALSE)
          AND (#{branchId}::bigint IS NULL OR n.branch_id IS NULL OR n.branch_id = #{branchId}::bigint)
        ORDER BY nr.created_at DESC, n.id DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<NotificationDTO> listForUser(@Param("userId") Long userId,
                                      @Param("onlyUnread") boolean onlyUnread,
                                      @Param("branchId") Long branchId,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    // ---------- read state ----------
    // is_read and read_at MUST be set together to satisfy
    // chk_notification_recipient_read_state.

    @Update("""
        UPDATE notification_recipients
        SET is_read = TRUE, read_at = NOW()
        WHERE user_id = #{userId}
          AND notification_id = #{notificationId}
          AND is_read = FALSE
        """)
    int markOneRead(@Param("userId") Long userId,
                    @Param("notificationId") Long notificationId);

    @Update("""
        UPDATE notification_recipients
        SET is_read = TRUE, read_at = NOW()
        WHERE user_id = #{userId}
          AND is_read = FALSE
        """)
    int markAllRead(@Param("userId") Long userId);
}
