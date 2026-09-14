package org.example.tnal_youth_backend.activity.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityStatusRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityStatusScheduler {

    private final ActivityRepository activityRepository;
    private final ActivityStatusRepository activityStatusRepository;
    private final ActivityInvitedBranchRepository activityInvitedBranchRepository;

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

    /*
     * Runs every minute, alongside the transition above.
     *
     * A branch invitation left PENDING once its activity has already
     * ended is auto-declined -- accepting an invitation to something that
     * already happened makes no sense, and leaving it PENDING would keep
     * showing an actionable Accept/Decline control (on the activity list
     * and the invited branch's notification) for nothing the invited
     * branch can still act on.
     *
     * ActivityServiceImpl#completeActivity already does this same decline
     * when staff manually mark an activity COMPLETED, but that's a manual
     * action nothing requires them to ever take -- the frontend's own
     * "effective status" already displays an activity as completed once
     * its end time passes, with or without that button being pressed. This
     * job is the actual, unconditional guarantee: keyed on endsAt, not on
     * whether anyone remembered to click Complete.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void declineStalePendingInvitations() {
        List<ActivityInvitedBranch> stalePendingInvitations =
                activityInvitedBranchRepository
                        .findAllByInvitationStatusAndActivity_EndsAtLessThanEqual(
                                ActivityInvitationStatus.PENDING,
                                OffsetDateTime.now()
                        );

        if (stalePendingInvitations.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        for (ActivityInvitedBranch invitation : stalePendingInvitations) {
            invitation.setInvitationStatus(
                    ActivityInvitationStatus.DECLINED
            );

            invitation.setRespondedAt(now);
        }

        activityInvitedBranchRepository.saveAll(
                stalePendingInvitations
        );
    }
}