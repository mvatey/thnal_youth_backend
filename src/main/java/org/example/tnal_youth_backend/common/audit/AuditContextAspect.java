package org.example.tnal_youth_backend.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Binds the current user id to the Postgres session variable app.current_user_id
 * on entry to any @Transactional method. The DB-side audit_trigger reads this
 * variable via current_setting('app.current_user_id', TRUE) to attribute writes
 * to the acting user.
 *
 * Without this aspect, EVERY audit_logs row across the entire schema records
 * user_id = NULL — the app has no other way to feed the trigger the current
 * user. See schema.sql section 14 for the trigger.
 *
 * Runs at LOWEST_PRECEDENCE so it fires AFTER Spring's transaction interceptor —
 * the transaction is already open, SET LOCAL is scoped to it, and the same
 * pooled connection is used.
 *
 * SET LOCAL cannot be parameterized in Postgres. userId is a Long — no injection
 * surface. Empty string is set for anonymous flows; the trigger's NULLIF handles
 * that gracefully.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Order // defaults to Ordered.LOWEST_PRECEDENCE
@Slf4j
public class AuditContextAspect {

    private final DataSource dataSource;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional) "
            + "|| @within(org.springframework.transaction.annotation.Transactional)")
    public void bindAuditContext() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }

        Long userId = null;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (BusinessException ignored) {
            // Anonymous flows (login, OTP) — audit records user_id=NULL, which is correct.
        }

        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (Statement st = conn.createStatement()) {
            if (userId != null) {
                st.execute("SET LOCAL app.current_user_id = '" + userId + "'");
            } else {
                st.execute("SET LOCAL app.current_user_id = ''");
            }
        } catch (SQLException e) {
            log.error("Failed to set audit context for user_id={}", userId, e);
            throw new IllegalStateException("Failed to set audit context", e);
        }
    }
}