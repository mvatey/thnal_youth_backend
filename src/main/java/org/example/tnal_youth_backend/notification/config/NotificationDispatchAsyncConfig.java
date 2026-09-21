package org.example.tnal_youth_backend.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/*
 * NotificationDispatchListener used to run AFTER_COMMIT on the same
 * request thread that triggered it -- fine for one recipient, but it
 * loops over every recipient sending email (no SMTP connection pooling,
 * ~1-3s each) and Telegram sequentially, so inviting several branches'
 * worth of staff/participants to an activity blocked that HTTP response
 * for 1-2 minutes. Marking the listener @Async lets the request return
 * as soon as its own transaction commits, with the actual sends
 * happening on this dedicated pool instead.
 *
 * A bounded pool (not Spring's default SimpleAsyncTaskExecutor, which
 * spawns an unbounded new thread per task) keeps a burst of invites from
 * opening an unbounded number of SMTP connections at once.
 */
@Configuration
@EnableAsync
public class NotificationDispatchAsyncConfig {

    @Bean(name = "notificationDispatchExecutor")
    public Executor notificationDispatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("notif-dispatch-");
        executor.initialize();
        return executor;
    }
}
