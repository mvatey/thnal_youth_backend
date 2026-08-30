package org.example.tnal_youth_backend.activity.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.notification.dispatch.NotificationCreatedEvent;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.example.tnal_youth_backend.notification.repo.NotificationRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends a reminder notification to every invitee of an activity that starts
 * "tomorrow" — i.e. exactly one calendar day from now — so they don't forget
 * to attend.
 *
 * <p>Runs unauthenticated (there is no logged-in user behind a scheduled
 * job), so unlike {@link org.example.tnal_youth_backend.notification.service.NotificationService#create}
 * this talks to {@link NotificationRepo} directly instead of going through
 * that service, which requires an authenticated actor for
 * {@code notifications.created_by}. A system-sent reminder simply leaves
 * {@code created_by} NULL, which the schema already allows (V11:
 * {@code ON DELETE SET NULL}).
 */
@Service
@RequiredArgsConstructor
public class ActivityReminderScheduler {

    private static final String ACTIVITY_REMINDER_TYPE_CODE = "ACTIVITY_REMINDER";

    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationRepo notificationRepo;
    private final ApplicationEventPublisher eventPublisher;

    /*
     * Runs every day at 14:00 (2pm) server time.
     *
     * Finds every activity whose startsAt falls within "tomorrow" (the next
     * full calendar day from now) and reminds every invitee who has not
     * already received a reminder for that activity.
     */
    @Scheduled(cron = "0 0 14 * * *")
    @Transactional
    public void sendEventReminders() {

        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime windowStart =
                now.plusDays(1)
                        .toLocalDate()
                        .atStartOfDay()
                        .atOffset(now.getOffset());

        OffsetDateTime windowEnd = windowStart.plusDays(1);

        List<Activity> activitiesStartingTomorrow =
                activityRepository
                        .findAllByStartsAtGreaterThanEqualAndStartsAtLessThan(
                                windowStart,
                                windowEnd
                        );

        for (Activity activity : activitiesStartingTomorrow) {
            if (isCancelled(activity)) {
                continue;
            }

            sendReminderForActivity(activity);
        }
    }

    private void sendReminderForActivity(Activity activity) {

        List<ActivityParticipant> unremindedParticipants =
                participantRepository
                        .findAllByActivity_IdAndReminderSentAtIsNull(
                                activity.getId()
                        );

        if (unremindedParticipants.isEmpty()) {
            return;
        }

        List<Long> userIds = new ArrayList<>();

        for (ActivityParticipant participant : unremindedParticipants) {
            Long memberId =
                    participant.getMember() != null
                            ? participant.getMember().getId()
                            : null;

            if (memberId == null) {
                continue;
            }

            userRepository
                    .findByMemberId(memberId)
                    .ifPresent(user -> userIds.add(user.getId()));
        }

        /*
         * Mark every unreminded participant as reminded regardless of
         * whether they have a linked user account, so members without an
         * account are not re-checked on every run.
         */
        OffsetDateTime sentAt = OffsetDateTime.now();

        for (ActivityParticipant participant : unremindedParticipants) {
            participant.setReminderSentAt(sentAt);
        }

        participantRepository.saveAll(unremindedParticipants);

        if (userIds.isEmpty()) {
            return;
        }

        Short typeId =
                notificationRepo.findActiveTypeIdByCode(
                        ACTIVITY_REMINDER_TYPE_CODE
                );

        if (typeId == null) {
            return;
        }

        String titleEn = activity.getTitleEn() != null && !activity.getTitleEn().isBlank()
                ? activity.getTitleEn()
                : activity.getTitleKm();

        NotificationModel notification =
                NotificationModel.builder()
                        .typeId(typeId)
                        .title("ការរំលឹកអំពីកម្មវិធីនាថ្ងៃស្អែក")
                        .body(
                                "កម្មវិធី \""
                                        + activity.getTitleKm()
                                        + "\" នឹងប្រព្រឹត្តទៅនៅថ្ងៃស្អែក។ "
                                        + "សូមរៀបចំខ្លួនអោយបានទាន់ពេលវេលា។"
                        )
                        .titleEn("Reminder: Activity Tomorrow")
                        .bodyEn(
                                "The activity \""
                                        + titleEn
                                        + "\" will take place tomorrow. "
                                        + "Please make sure you are ready on time."
                        )
                        .actionUrl("/activity/" + activity.getId())
                        .activityId(activity.getId())
                        .createdBy(null)
                        .build();

        notificationRepo.insertNotification(notification);

        Long notificationId = notification.getId();

        if (notificationId == null) {
            return;
        }

        notificationRepo.fanOutUsers(notificationId, userIds);

        // Same AFTER_COMMIT-dispatched channel fan-out as
        // NotificationService#create — this scheduler bypasses that
        // service (see the class doc comment) but reminders still need
        // email/Telegram delivery, so the event is published here too.
        eventPublisher.publishEvent(new NotificationCreatedEvent(notificationId));
    }

    private boolean isCancelled(Activity activity) {
        if (activity.getStatus() == null
                || activity.getStatus().getCode() == null) {
            return false;
        }

        return "CANCELLED".equals(
                activity.getStatus()
                        .getCode()
                        .trim()
                        .toUpperCase()
        );
    }
}
