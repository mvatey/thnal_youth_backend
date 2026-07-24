package org.example.tnal_youth_backend.activity.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityStatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ActivityStatusScheduler {

    private final ActivityRepository activityRepository;
    private final ActivityStatusRepository activityStatusRepository;

    /*
     * Runs every minute.
     *
     * UPCOMING activities whose startsAt has arrived
     * automatically become ONGOING.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateUpcomingActivitiesToOngoing() {

        ActivityStatus ongoingStatus =
                activityStatusRepository
                        .findByCodeIgnoreCase("ONGOING")
                        .filter(status ->
                                Boolean.TRUE.equals(
                                        status.getActive()
                                )
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "ONGOING activity status is missing or inactive"
                                )
                        );

        activityRepository
                .findAllByStatus_CodeIgnoreCaseAndStartsAtLessThanEqual(
                        "UPCOMING",
                        OffsetDateTime.now()
                )
                .forEach(activity ->
                        activity.setStatus(ongoingStatus)
                );
    }
}