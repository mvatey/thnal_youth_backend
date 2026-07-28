package org.example.tnal_youth_backend.notification.repo;

import org.example.tnal_youth_backend.notification.dto.NotificationDTO;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@MybatisTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration"
})
class NotificationRepoIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private NotificationRepo repo;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;

    private short typeId;
    private List<Long> activeUserIds;
    private long actorId;

    // Keep the remaining existing test methods.

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);

        Short loadedTypeId = jdbc.queryForObject(
                """
                SELECT id
                FROM notification_types
                WHERE is_active = TRUE
                ORDER BY id
                LIMIT 1
                """,
                Short.class
        );

        typeId = Objects.requireNonNull(
                loadedTypeId,
                "Flyway migrations must seed at least one active notification type"
        );

        activeUserIds = jdbc.queryForList(
                """
                SELECT id
                FROM users
                WHERE status = 'ACTIVE'
                ORDER BY id
                LIMIT 3
                """,
                Long.class
        );

        assertThat(activeUserIds)
                .as("Flyway migrations must seed at least 2 ACTIVE users")
                .hasSizeGreaterThanOrEqualTo(2);

        actorId = activeUserIds.getFirst();
    }

    @Test
    void insertNotification_populatesGeneratedIdAndCreatedAt() {
        NotificationModel notification = baseNotification().build();

        int rows = repo.insertNotification(notification);

        assertThat(rows).isEqualTo(1);

        assertThat(notification.getId())
                .as("generated notification ID")
                .isNotNull();

        assertThat(notification.getCreatedAt())
                .as("database-generated created_at")
                .isNotNull();
    }

    @Test
    void fanOutUsers_insertsOneRecipientPerActiveUser_andListForUserReturnsIt() {
        NotificationModel notification = baseNotification().build();

        repo.insertNotification(notification);

        Long notificationId = notification.getId();

        int inserted = repo.fanOutUsers(
                notificationId,
                activeUserIds
        );

        assertThat(inserted)
                .isEqualTo(activeUserIds.size());

        /*
         * ON CONFLICT DO NOTHING should make duplicate fan-out
         * return zero affected rows.
         */
        int duplicateInsert = repo.fanOutUsers(
                notificationId,
                activeUserIds
        );

        assertThat(duplicateInsert).isZero();

        Long recipientId = activeUserIds.getFirst();

        List<NotificationDTO> inbox = repo.listForUser(
                recipientId,
                false,
                20,
                0
        );

        assertThat(inbox)
                .extracting(NotificationDTO::getId)
                .contains(notificationId);

        NotificationDTO row = inbox.stream()
                .filter(dto -> dto.getId().equals(notificationId))
                .findFirst()
                .orElseThrow();

        assertThat(row.getTypeCode())
                .as("notification type join should resolve")
                .isNotBlank();

        assertThat(row.isRead()).isFalse();
        assertThat(row.getReadAt()).isNull();
    }

    @Test
    void markOneRead_flipsReadStateTogether_andIsIdempotent() {
        NotificationModel notification = baseNotification().build();

        repo.insertNotification(notification);

        Long notificationId = notification.getId();
        Long recipientId = activeUserIds.getFirst();

        repo.fanOutUsers(
                notificationId,
                List.of(recipientId)
        );

        assertThat(repo.countUnread(recipientId))
                .isGreaterThanOrEqualTo(1);

        int firstMark = repo.markOneRead(
                recipientId,
                notificationId
        );

        assertThat(firstMark).isEqualTo(1);

        /*
         * A second update should affect zero rows because the recipient
         * has already read the notification.
         */
        int secondMark = repo.markOneRead(
                recipientId,
                notificationId
        );

        assertThat(secondMark).isZero();

        Boolean isRead = jdbc.queryForObject(
                """
                SELECT is_read
                FROM notification_recipients
                WHERE notification_id = ?
                  AND user_id = ?
                """,
                Boolean.class,
                notificationId,
                recipientId
        );

        Object readAt = jdbc.queryForObject(
                """
                SELECT read_at
                FROM notification_recipients
                WHERE notification_id = ?
                  AND user_id = ?
                """,
                Object.class,
                notificationId,
                recipientId
        );

        assertThat(isRead).isTrue();
        assertThat(readAt).isNotNull();
    }

    @Test
    void markAllRead_marksEveryUnreadForUser() {
        Long recipientId = activeUserIds.get(1);

        NotificationModel firstNotification =
                baseNotification().build();

        repo.insertNotification(firstNotification);

        repo.fanOutUsers(
                firstNotification.getId(),
                List.of(recipientId)
        );

        NotificationModel secondNotification =
                baseNotification().build();

        repo.insertNotification(secondNotification);

        repo.fanOutUsers(
                secondNotification.getId(),
                List.of(recipientId)
        );

        long unreadBefore = repo.countUnread(recipientId);

        assertThat(unreadBefore)
                .isGreaterThanOrEqualTo(2);

        int marked = repo.markAllRead(recipientId);

        assertThat(marked)
                .isEqualTo(Math.toIntExact(unreadBefore));

        assertThat(repo.countUnread(recipientId))
                .isZero();
    }

    @Test
    void onlyUnreadFilter_appliesBooleanCastCorrectly() {
        Long recipientId = activeUserIds.getFirst();

        NotificationModel notification =
                baseNotification().build();

        repo.insertNotification(notification);

        repo.fanOutUsers(
                notification.getId(),
                List.of(recipientId)
        );

        long allBefore = repo.countForUser(
                recipientId,
                false
        );

        long unreadBefore = repo.countForUser(
                recipientId,
                true
        );

        assertThat(allBefore)
                .isGreaterThanOrEqualTo(unreadBefore);

        repo.markOneRead(
                recipientId,
                notification.getId()
        );

        assertThat(repo.countForUser(recipientId, true))
                .isEqualTo(unreadBefore - 1);

        assertThat(repo.countForUser(recipientId, false))
                .isEqualTo(allBefore);
    }

    // ============================================================
    // Idempotency
    // ============================================================

    @Test
    void clientRequestId_lookupFindsTheOriginal() {
        String requestId = UUID.randomUUID().toString();

        NotificationModel notification = baseNotification()
                .clientRequestId(requestId)
                .build();

        repo.insertNotification(notification);

        Long foundId = repo.findIdByCreatorAndClientRequestId(
                actorId,
                requestId
        );

        assertThat(foundId)
                .isEqualTo(notification.getId());

        assertThat(repo.findCreatedAtById(notification.getId()))
                .isNotNull();
    }

    @Test
    void clientRequestId_duplicateForSameCreator_violatesUniqueIndex() {
        String requestId = UUID.randomUUID().toString();

        NotificationModel firstNotification = baseNotification()
                .clientRequestId(requestId)
                .build();

        repo.insertNotification(firstNotification);

        NotificationModel duplicateNotification = baseNotification()
                .clientRequestId(requestId)
                .build();

        assertThatThrownBy(
                () -> repo.insertNotification(duplicateNotification)
        ).isInstanceOf(
                org.springframework.dao.DataIntegrityViolationException.class
        );
    }

    @Test
    void nullClientRequestId_isExemptFromUniqueIndex() {
        NotificationModel firstNotification =
                baseNotification().build();

        NotificationModel secondNotification =
                baseNotification().build();

        repo.insertNotification(firstNotification);
        repo.insertNotification(secondNotification);

        assertThat(firstNotification.getId())
                .isNotEqualTo(secondNotification.getId());
    }

    // ============================================================
    // Helpers
    // ============================================================

    private NotificationModel.NotificationModelBuilder baseNotification() {
        return NotificationModel.builder()
                .typeId(typeId)
                .title("Integration test")
                .body("Body")
                .actionUrl("/it/1")
                .createdBy(actorId);
    }
}