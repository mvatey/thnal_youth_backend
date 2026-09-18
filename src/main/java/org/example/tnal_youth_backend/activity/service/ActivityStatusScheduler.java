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
     * Runs every minute, alongside the other transitions in this class.
     *
     * UPCOMING or ONGOING activities whose endsAt has passed automatically
     * become COMPLETED -- previously this only ever happened when staff
     * manually clicked Complete (see ActivityServiceImpl#completeActivity),
     * which nothing required them to do. The frontend's own "effective
     * status" already displayed a past-endsAt activity as completed
     * regardless of the real stored status, which meant anything reading
     * the actual database status directly (the dashboard's Recent
     * Completed count, notably) silently fell behind whenever nobody
     * pressed the button. This keeps the real status in sync with what
     * the UI already implied, the same way the ONGOING transition above
     * keeps UPCOMING from lingering past its startsAt.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateEndedActivitiesToCompleted() {

        ActivityStatus completedStatus =
                activityStatusRepository
                        .findByCodeIgnoreCase("COMPLETED")
                        .filter(status ->
                                Boolean.TRUE.equals(
                                        status.getActive()
                                )
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "COMPLETED activity status is missing or inactive"
                                )
                        );

        activityRepository
                .findAllByStatus_CodeInAndEndsAtLessThanEqual(
                        List.of("UPCOMING", "ONGOING"),
                        OffsetDateTime.now()
                )
                .forEach(activity ->
                        activity.setStatus(completedStatus)
                );
    }

    /*
     * Runs every minute, alongside the transitions above.
     *
     * A branch invitation left PENDING once its activity has already
     * ended is auto-declined -- accepting an invitation to something that
     * already happened makes no sense, and leaving it PENDING would keep
     * showing an actionable Accept/Decline control (on the activity list
     * and the invited branch's notification) for nothing the invited
     * branch can still act on. Now redundant with
     * updateEndedActivitiesToCompleted() above for most cases (completing
     * an activity already declines its stale invitations via
     * ActivityServiceImpl#completeActivity), but kept as the same
     * unconditional, endsAt-keyed guarantee regardless of which job
     * happens to run first within the same minute.
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