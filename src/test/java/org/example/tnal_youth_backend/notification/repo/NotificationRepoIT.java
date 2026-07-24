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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real-database integration test for {@link NotificationRepo}.
 *
 * <p>This is the coverage that was previously missing: every other test mocks
 * {@link org.example.tnal_youth_backend.notification.service.NotificationService},
 * so none of the actual MyBatis SQL, the multi-column generated key, the
 * {@code ::boolean} cast, {@code ON CONFLICT}, or the read-state CHECK were ever
 * executed in CI.
 *
 * <p>Runs the project's real Flyway migrations (V1..V21) against a throwaway
 * Postgres 16 container, so the schema under test is byte-for-byte what ships.
 *
 * <p>REQUIREMENTS: a Docker-capable CI runner. Testcontainers pulls
 * {@code postgres:16-alpine} on first run. NOTE: authored but NOT executed in the
 * authoring sandbox (no Docker there) — run via {@code mvn -Dtest=NotificationRepoIT test}.
 */
@Testcontainers
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

    /** An active notification type id and a couple of active user ids from the seeded data. */
    private short typeId;
    private List<Long> activeUserIds;
    private long actorId;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);

        typeId = jdbc.queryForObject(
                "SELECT id FROM notification_types WHERE is_active = TRUE ORDER BY id LIMIT 1",
                Short.class);

        activeUserIds = jdbc.queryForList(
                "SELECT id FROM users WHERE status = 'ACTIVE' ORDER BY id LIMIT 3",
                Long.class);

        assertThat(activeUserIds)
                .as("migrations must seed at least 2 ACTIVE users for fan-out assertions")
                .hasSizeGreaterThanOrEqualTo(2);

        actorId = activeUserIds.get(0);
    }

    @Test
    void insertNotification_populatesGeneratedIdAndCreatedAt() {
        NotificationModel n = baseNotification().build();

        int rows = repo.insertNotification(n);

        assertThat(rows).isEqualTo(1);
        // The multi-column @Options(keyProperty = "id,createdAt") must populate BOTH.
        assertThat(n.getId()).as("generated id").isNotNull();
        assertThat(n.getCreatedAt()).as("DB-defaulted created_at").isNotNull();
    }

    @Test
    void fanOutUsers_insertsOneRecipientPerActiveUser_andListForUserReturnsIt() {
        NotificationModel n = baseNotification().build();
        repo.insertNotification(n);
        Long nid = n.getId();

        int inserted = repo.fanOutUsers(nid, activeUserIds);
        assertThat(inserted).isEqualTo(activeUserIds.size());

        // Duplicate fan-out is a no-op thanks to ON CONFLICT DO NOTHING.
        int again = repo.fanOutUsers(nid, activeUserIds);
        assertThat(again).isZero();

        Long recipient = activeUserIds.get(0);
        List<NotificationDTO> inbox = repo.listForUser(recipient, false, 20, 0);
        assertThat(inbox).extracting(NotificationDTO::getId).contains(nid);

        NotificationDTO row = inbox.stream().filter(d -> d.getId().equals(nid)).findFirst().orElseThrow();
        assertThat(row.getTypeCode()).isNotBlank();   // join to notification_types resolved
        assertThat(row.isRead()).isFalse();
        assertThat(row.getReadAt()).isNull();
    }

    @Test
    void markOneRead_flipsReadStateTogether_andIsIdempotent() {
        NotificationModel n = baseNotification().build();
        repo.insertNotification(n);
        Long nid = n.getId();
        Long recipient = activeUserIds.get(0);
        repo.fanOutUsers(nid, List.of(recipient));

        assertThat(repo.countUnread(recipient)).isGreaterThanOrEqualTo(1);

        int firstMark = repo.markOneRead(recipient, nid);
        assertThat(firstMark).isEqualTo(1);

        // Second mark is a no-op (WHERE is_read = FALSE) — no CHECK violation.
        int secondMark = repo.markOneRead(recipient, nid);
        assertThat(secondMark).isZero();

        // read_at must be non-null now (CHECK: is_read TRUE => read_at NOT NULL).
        Boolean isRead = jdbc.queryForObject(
                "SELECT is_read FROM notification_recipients WHERE notification_id = ? AND user_id = ?",
                Boolean.class, nid, recipient);
        Object readAt = jdbc.queryForObject(
                "SELECT read_at FROM notification_recipients WHERE notification_id = ? AND user_id = ?",
                Object.class, nid, recipient);
        assertThat(isRead).isTrue();
        assertThat(readAt).isNotNull();
    }

    @Test
    void markAllRead_marksEveryUnreadForUser() {
        Long recipient = activeUserIds.get(1);

        NotificationModel a = baseNotification().build();
        repo.insertNotification(a);
        repo.fanOutUsers(a.getId(), List.of(recipient));

        NotificationModel b = baseNotification().build();
        repo.insertNotification(b);
        repo.fanOutUsers(b.getId(), List.of(recipient));

        long unreadBefore = repo.countUnread(recipient);
        assertThat(unreadBefore).isGreaterThanOrEqualTo(2);

        int marked = repo.markAllRead(recipient);
        assertThat(marked).isEqualTo((int) unreadBefore);
        assertThat(repo.countUnread(recipient)).isZero();
    }

    @Test
    void onlyUnreadFilter_appliesBooleanCastCorrectly() {
        Long recipient = activeUserIds.get(0);

        NotificationModel n = baseNotification().build();
        repo.insertNotification(n);
        repo.fanOutUsers(n.getId(), List.of(recipient));

        long allBefore = repo.countForUser(recipient, false);
        long unreadBefore = repo.countForUser(recipient, true);
        assertThat(allBefore).isGreaterThanOrEqualTo(unreadBefore);

        repo.markOneRead(recipient, n.getId());

        assertThat(repo.countForUser(recipient, true)).isEqualTo(unreadBefore - 1);
        assertThat(repo.countForUser(recipient, false)).isEqualTo(allBefore);
    }

    // ------------------------------------------------------------ idempotency (V21)

    @Test
    void clientRequestId_lookupFindsTheOriginal() {
        String key = UUID.randomUUID().toString();
        NotificationModel n = baseNotification().clientRequestId(key).build();
        repo.insertNotification(n);

        Long found = repo.findIdByCreatorAndClientRequestId(actorId, key);
        assertThat(found).isEqualTo(n.getId());

        assertThat(repo.findCreatedAtById(n.getId())).isNotNull();
    }

    @Test
    void clientRequestId_duplicateForSameCreator_violatesUniqueIndex() {
        String key = UUID.randomUUID().toString();

        NotificationModel first = baseNotification().clientRequestId(key).build();
        repo.insertNotification(first);

        NotificationModel dup = baseNotification().clientRequestId(key).build();
        // Same (created_by, client_request_id) -> partial unique index rejects it.
        assertThatThrownBy(() -> repo.insertNotification(dup))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void nullClientRequestId_isExemptFromUniqueIndex() {
        // Two notifications with NULL keys must both succeed (partial index skips NULLs).
        NotificationModel a = baseNotification().build();
        NotificationModel b = baseNotification().build();
        repo.insertNotification(a);
        repo.insertNotification(b);
        assertThat(a.getId()).isNotEqualTo(b.getId());
    }

    // ---------------------------------------------------------------- helpers

    private NotificationModel.NotificationModelBuilder baseNotification() {
        return NotificationModel.builder()
                .typeId(typeId)
                .title("Integration test")
                .body("Body")
                .actionUrl("/it/1")
                .createdBy(actorId);
    }
}
