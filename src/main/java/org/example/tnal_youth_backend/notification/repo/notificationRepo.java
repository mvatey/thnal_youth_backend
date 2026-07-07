package org.example.tnal_youth_backend.notification.repo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.tnal_youth_backend.notification.dto.notificationDTO;
import org.example.tnal_youth_backend.notification.model.notificationModel;

import java.util.List;

@Mapper
public interface notificationRepo {

    // ---------- notifications ----------

    @Insert("""
        INSERT INTO notifications
            (type_id, title, body, link_url, program_id, branch_id,
             sent_via_in_app, sent_via_sms, sent_via_email, created_by)
        VALUES
            (#{typeId}, #{title}, #{body}, #{linkUrl}, #{programId}, #{branchId},
             #{sentViaInApp}, #{sentViaSms}, #{sentViaEmail}, #{createdBy})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertNotification(notificationModel n);

    // ---------- recipient fan-out ----------
    // One statement, one round-trip per target mode. Fewer rows in flight than
    // per-user inserts; DB does the set expansion.

    @Insert("""
        INSERT INTO notification_recipients (notification_id, user_id)
        SELECT #{nid}, u.id
        FROM users u
        WHERE u.status = 'ACTIVE'
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """)
    int fanOutAll(@Param("nid") Long notificationId);

    @Insert("""
        INSERT INTO notification_recipients (notification_id, user_id)
        SELECT #{nid}, m.user_id
        FROM members m
        JOIN member_statuses ms ON ms.id = m.status_id
        WHERE m.branch_id = #{branchId}
          AND m.user_id IS NOT NULL
          AND ms.code <> 'INACTIVE'
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """)
    int fanOutBranch(@Param("nid") Long notificationId,
                     @Param("branchId") Long branchId);

    @Insert("""
        INSERT INTO notification_recipients (notification_id, user_id)
        SELECT DISTINCT #{nid}, m.user_id
        FROM program_participants pp
        JOIN members m ON m.id = pp.member_id
        WHERE pp.program_id = #{programId}
          AND m.user_id IS NOT NULL
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """)
    int fanOutProgramParticipants(@Param("nid") Long notificationId,
                                  @Param("programId") Long programId);

    @Insert({
            "<script>",
            "INSERT INTO notification_recipients (notification_id, user_id) VALUES ",
            "<foreach collection='userIds' item='uid' separator=','>",
            "  (#{nid}, #{uid})",
            "</foreach>",
            "ON CONFLICT (notification_id, user_id) DO NOTHING",
            "</script>"
    })
    int fanOutUsers(@Param("nid") Long notificationId,
                    @Param("userIds") List<Long> userIds);

    // ---------- inbox reads ----------

    @Select("""
        SELECT COUNT(*)
        FROM notification_recipients
        WHERE user_id = #{userId} AND read_at IS NULL
        """)
    long countUnread(@Param("userId") Long userId);

    @Select("""
        SELECT
            n.id            AS id,
            nt.code         AS typeCode,
            nt.label_km     AS typeLabelKm,
            nt.label_en     AS typeLabelEn,
            n.title         AS title,
            n.body          AS body,
            n.link_url      AS linkUrl,
            n.program_id    AS programId,
            n.branch_id     AS branchId,
            n.created_at    AS createdAt,
            nr.read_at      AS readAt
        FROM notification_recipients nr
        JOIN notifications      n  ON n.id = nr.notification_id
        JOIN notification_types nt ON nt.id = n.type_id
        WHERE nr.user_id = #{userId}
          AND (#{onlyUnread}::boolean = FALSE OR nr.read_at IS NULL)
        ORDER BY n.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<notificationDTO> listForUser(@Param("userId") Long userId,
                                      @Param("onlyUnread") boolean onlyUnread,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    // ---------- read state ----------

    @Update("""
        UPDATE notification_recipients
        SET read_at = NOW()
        WHERE user_id = #{userId}
          AND notification_id = #{notificationId}
          AND read_at IS NULL
        """)
    int markOneRead(@Param("userId") Long userId,
                    @Param("notificationId") Long notificationId);

    @Update("""
        UPDATE notification_recipients
        SET read_at = NOW()
        WHERE user_id = #{userId}
          AND read_at IS NULL
        """)
    int markAllRead(@Param("userId") Long userId);
}