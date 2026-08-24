package org.example.tnal_youth_backend.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.mapper.ActivityMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.example.tnal_youth_backend.activity.model.entity.ActivityStatus;
import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
import org.example.tnal_youth_backend.activity.model.request.CreateActivityRequest;
import org.example.tnal_youth_backend.activity.model.request.UpdateActivityRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityListItemResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityPageResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityResponse;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.repository.ActivitySectorRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityStatusRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityTypeRepository;
import org.example.tnal_youth_backend.activity.service.ActivityService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.lookup.repository.ProvinceRepository;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final StaffBranchScopeService staffBranchScopeService;

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivitySectorRepository activitySectorRepository;
    private final ActivityStatusRepository activityStatusRepository;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;
    private final BranchRepository branchRepository;
    private final ProvinceRepository provinceRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final ActivityInvitedBranchRepository activityInvitedBranchRepository;

    @Override
    @Transactional
    public ActivityResponse createActivity(
            CreateActivityRequest request,
            Long currentUserId
    ) {
        validateCreateRequest(request);

        ActivityType activityType =
                getActiveActivityType(request.getTypeId());

        ActivitySector activitySector =
                getActiveActivitySector(request.getSectorId());

        ActivityStatus activityStatus =
                resolveInitialStatus(request);

        /*
         * Visibility follows the organization branch flow.
         *
         * INTERNAL:
         * - members from the activity's own branch can see it
         * - individual invitations are limited to the same branch
         *
         * EXTERNAL:
         * - members from the activity's own branch can see it
         * - other branches can be invited separately later
         *
         * Neither type is automatically visible to everybody.
         */
        boolean publicActivity = false;

        Activity activity = activityMapper.toEntity(
                request,
                activityType,
                activitySector,
                activityStatus,
                publicActivity,
                currentUserId
        );

        Activity savedActivity =
                activityRepository.save(activity);

        ActivityResponse response = activityMapper.toResponse(savedActivity);
        populateBranchName(response, savedActivity.getBranchId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(
            Long activityId,
            Long branchId,
            Long currentUserId
    ) {
        Activity activity = getActivity(activityId);

        User currentUser = requireUser(currentUserId);

        /*
         * MEMBER visibility follows the same rule as the activity list:
         *
         *   1) activities hosted by the member's own/home branch, OR
         *   2) activities where the member was personally invited/added
         *      as a participant.
         *
         * This keeps ordinary branch activities visible to every member of
         * that branch while still allowing a member to see a cross-branch
         * activity they were explicitly invited to.
         */
        if (currentUser.getRole() == UserRole.MEMBER) {
            boolean sameBranch =
                    currentUser.getBranchId() != null
                            && activity.getBranchId() != null
                            && currentUser.getBranchId()
                            .equals(activity.getBranchId());

            boolean personallyInvited =
                    currentUser.getMemberId() != null
                            && activityParticipantRepository
                            .existsByActivity_IdAndMember_Id(
                                    activityId,
                                    currentUser.getMemberId()
                            );

            if (!sameBranch && !personallyInvited) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view activities from your branch "
                                + "or activities you were invited to"
                );
            }
        }

        ActivityResponse response =
                activityMapper.toResponse(activity);

        populateBranchName(response, activity.getBranchId());

        // Keep the detail card consistent with the activity list:
        // member_joined / invited for every role. Capacity is not invitation count.
        activityParticipantRepository
                .countAttendanceGroupedByActivityIds(List.of(activityId))
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        counts -> {
                            response.setJoinedCount(
                                    counts.getJoinedCount() == null ? 0L : counts.getJoinedCount()
                            );
                            response.setInvitedCount(
                                    counts.getInvitedCount() == null ? 0L : counts.getInvitedCount()
                            );
                        },
                        () -> {
                            response.setJoinedCount(0L);
                            response.setInvitedCount(0L);
                        }
                );

        resolveCreatedByDisplay(activity, response);

        boolean canManage;
        Long invitedBranchId = null;

        if (branchId != null
                && (currentUser.getRole() == UserRole.SECRETARY
                || currentUser.getRole() == UserRole.BRANCH_LEADER)) {
            // The sidebar-selected branch is the permission context.
            // Being staff of another branch must not make the user an
            // organizer while viewing an invited branch's activity.
            validateExplicitBranchAccess(currentUser, branchId);

            boolean hostOfSelectedBranch =
                    branchId.equals(activity.getBranchId());

            canManage = hostOfSelectedBranch
                    && computeCanManage(currentUser, activity);

            if (!canManage) {
                boolean acceptedInvitation =
                        activityInvitedBranchRepository
                                .findByActivity_IdAndBranch_IdAndInvitationStatus(
                                        activity.getId(),
                                        branchId,
                                        ActivityInvitationStatus.ACCEPTED
                                )
                                .isPresent();

                if (acceptedInvitation) {
                    invitedBranchId = branchId;
                }
            }
        } else {
            canManage = computeCanManage(currentUser, activity);
            if (!canManage) {
                invitedBranchId =
                        resolveManagedInvitedBranchId(currentUser, activity);
            }
        }

        response.setCanManage(canManage);
        response.setCanManageAsInvitedBranch(invitedBranchId != null);
        response.setManagedInvitedBranchId(invitedBranchId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityPageResponse getActivities(
            int page,
            int size,
            String search,
            Short sectorId,
            Short typeId,
            LocalDate date,
            Long branchId,
            Long currentUserId
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be greater than zero"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "startsAt"
                )
        );

        /*
         * Search and filter repository logic can be added later.
         * For now, this preserves the existing pagination behavior.
         */
        Page<Activity> activityPage;
        Set<Long> ownBranchIdsForScope = null;
        Long invitedActivityCountForResponse = null;
        Map<Long, ActivityInvitedBranch> invitedBranchByActivityId = null;
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user could not be found"
                ));

        if (branchId != null) {
            /*
             * An explicit branchId narrows the list to exactly that
             * branch's own-hosted activities plus activities it was
             * invited to and accepted — regardless of the caller's normal
             * role-based scope. Used by callers (e.g. the activity-donation
             * module) that let staff pick any ONE of their accessible
             * branches and need that specific branch's activities, not the
             * caller's full combined scope.
             */
            validateExplicitBranchAccess(currentUser, branchId);

            Set<Long> branchIds = Set.of(branchId);
            ownBranchIdsForScope = branchIds;

            // A selected sidebar branch must see both pending and accepted
            // invitations. Pending invitations are needed on the Activity
            // page so the user can Accept/Decline without relying only on
            // the notification.
            List<ActivityInvitedBranch> invitations =
                    activityInvitedBranchRepository
                            .findByBranchIdInAndInvitationStatusIn(
                                    branchIds,
                                    List.of(
                                            ActivityInvitationStatus.PENDING,
                                            ActivityInvitationStatus.ACCEPTED,
                                            ActivityInvitationStatus.DECLINED
                                    )
                            );

            Map<Long, ActivityInvitedBranch> invitedBranchByActivityIdLocal =
                    new LinkedHashMap<>();

            for (ActivityInvitedBranch invitation : invitations) {
                Long invitationActivityId =
                        invitation.getActivity().getId();
                ActivityInvitedBranch existing =
                        invitedBranchByActivityIdLocal.get(invitationActivityId);

                boolean shouldReplace =
                        existing == null
                                || (existing.getInvitationStatus()
                                != ActivityInvitationStatus.ACCEPTED
                                && invitation.getInvitationStatus()
                                == ActivityInvitationStatus.ACCEPTED);

                if (shouldReplace) {
                    invitedBranchByActivityIdLocal.put(
                            invitationActivityId,
                            invitation
                    );
                }
            }

            invitedBranchByActivityId = invitedBranchByActivityIdLocal;

            Set<Long> invitedActivityIds =
                    invitedBranchByActivityIdLocal.keySet();

            invitedActivityCountForResponse =
                    (long) invitedActivityIds.size();

            activityPage = invitedActivityIds.isEmpty()
                    ? activityRepository.findAllByBranchIdIn(
                            branchIds,
                            pageable
                    )
                    : activityRepository.findAllByBranchIdInOrIdIn(
                            branchIds,
                            invitedActivityIds,
                            pageable
                    );
        } else if (currentUser.getRole() == UserRole.SECRETARY
                || currentUser.getRole() == UserRole.BRANCH_LEADER) {
            /*
             * Both a SECRETARY and a BRANCH_LEADER can respond to a branch
             * co-hosting invitation (see
             * ActivityInvitedBranchServiceImpl#validateInvitedBranchPermission),
             * so both need this same own/invited scoping to actually see
             * the tabs, the pending badge, and the accept/decline action on
             * this list -- previously only SECRETARY reached this branch,
             * so a branch leader's list fell through to the unscoped
             * "every activity in the system" branch below and never showed
             * ownBranch at all (see ActivityListItemResponse#ownBranch).
             */
            Set<Long> branchIds = resolveOwnBranchIds(currentUser);
            ownBranchIdsForScope = branchIds;

            /*
             * A secretary's/branch leader's activity list should also
             * include activities hosted by a DIFFERENT branch that has
             * invited one of their own branches to co-host — previously the
             * list only ever queried by host branchId, so an accepted
             * co-hosting invitation never actually surfaced the activity
             * here (only reachable via the activity's own detail-page URL,
             * e.g. from the invitation notification).
             *
             * PENDING is included too (not just ACCEPTED) so an invitation
             * that hasn't been responded to yet shows up here with an
             * Accept/Decline action, instead of only being reachable via
             * the notification link. DECLINED is also included — a declined
             * invitation stays visible as a historical row (rendered in red
             * on the frontend) instead of vanishing from the list, so staff
             * can still see which invitations they turned down. CANCELLED
             * (the host withdrawing the invitation) is the one status still
             * excluded — the frontend shows a distinct action per status
             * (see ActivityListItemResponse.invitationStatus). This is
             * deliberately broader than the explicit-branchId path above,
             * which stays ACCEPTED-only — recording a donation against an
             * activity your branch hasn't actually confirmed co-hosting yet
             * doesn't make sense, so that path is not widened here.
             */
            List<ActivityInvitedBranch> invitations =
                    activityInvitedBranchRepository
                            .findByBranchIdInAndInvitationStatusIn(
                                    branchIds,
                                    List.of(
                                            ActivityInvitationStatus.PENDING,
                                            ActivityInvitationStatus.ACCEPTED,
                                            ActivityInvitationStatus.DECLINED
                                    )
                            );

            Map<Long, ActivityInvitedBranch> invitedBranchByActivityIdLocal =
                    new LinkedHashMap<>();

            for (ActivityInvitedBranch invitation : invitations) {
                Long invitationActivityId =
                        invitation.getActivity().getId();

                ActivityInvitedBranch existing =
                        invitedBranchByActivityIdLocal.get(
                                invitationActivityId
                        );

                /*
                 * An activity should never have more than one active
                 * invitation to the same set of the viewer's own branches,
                 * but if it somehow does, prefer showing the ACCEPTED one
                 * over a stray PENDING one.
                 */
                boolean shouldReplace =
                        existing == null
                                || (existing.getInvitationStatus()
                                != ActivityInvitationStatus.ACCEPTED
                                && invitation.getInvitationStatus()
                                == ActivityInvitationStatus.ACCEPTED);

                if (shouldReplace) {
                    invitedBranchByActivityIdLocal.put(
                            invitationActivityId,
                            invitation
                    );
                }
            }

            invitedBranchByActivityId = invitedBranchByActivityIdLocal;

            Set<Long> invitedActivityIds =
                    invitedBranchByActivityIdLocal.keySet();

            invitedActivityCountForResponse =
                    (long) invitedActivityIds.size();

            activityPage = invitedActivityIds.isEmpty()
                    ? activityRepository.findAllByBranchIdIn(
                            branchIds,
                            pageable
                    )
                    : activityRepository.findAllByBranchIdInOrIdIn(
                            branchIds,
                            invitedActivityIds,
                            pageable
                    );
        } else if (currentUser.getRole() == UserRole.MEMBER) {
            /*
             * MEMBER sees exactly:
             *
             *   - activities hosted by their own/home branch, plus
             *   - activities where they were personally invited/added as
             *     a participant (including activities hosted elsewhere).
             *
             * They never receive the organization-wide activity catalog.
             */
            Set<Long> ownBranchIds =
                    currentUser.getBranchId() == null
                            ? Set.of()
                            : Set.of(currentUser.getBranchId());

            List<Long> personallyInvitedActivityIds =
                    currentUser.getMemberId() == null
                            ? List.of()
                            : activityParticipantRepository
                            .findDistinctActivityIdsByMemberId(
                                    currentUser.getMemberId()
                            );

            Set<Long> invitedIds =
                    new LinkedHashSet<>(personallyInvitedActivityIds);

            if (ownBranchIds.isEmpty()) {
                activityPage = invitedIds.isEmpty()
                        ? Page.empty(pageable)
                        : activityRepository.findAllByIdIn(
                                invitedIds,
                                pageable
                        );
            } else if (invitedIds.isEmpty()) {
                activityPage = activityRepository.findAllByBranchIdIn(
                        ownBranchIds,
                        pageable
                );
            } else {
                activityPage = activityRepository.findAllByBranchIdInOrIdIn(
                        ownBranchIds,
                        invitedIds,
                        pageable
                );
            }
        } else {
            activityPage = activityRepository.findAll(pageable);
        }

        List<Long> pageActivityIds = activityPage.getContent()
                .stream()
                .map(Activity::getId)
                .toList();

        /*
         * Batched: one count-per-activity query for the whole page, instead
         * of one query per row. Counts every participant regardless of
         * source (host branch, an accepted co-hosting branch, or a
         * walk-in) — see ActivityParticipantRepository
         * .countGroupedByActivityIds.
         */
        Map<Long, Long> participantCountsByActivityId =
                pageActivityIds.isEmpty()
                        ? Map.of()
                        : activityParticipantRepository
                                .countGroupedByActivityIds(pageActivityIds)
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                ActivityParticipantRepository
                                                        .ActivityParticipantCountProjection
                                                        ::getActivityId,
                                                ActivityParticipantRepository
                                                        .ActivityParticipantCountProjection
                                                        ::getParticipantCount
                                        )
                                );

        Map<Long, ActivityParticipantRepository.ActivityAttendanceCountProjection>
                attendanceCountsByActivityId =
                pageActivityIds.isEmpty()
                        ? Map.of()
                        : activityParticipantRepository
                                .countAttendanceGroupedByActivityIds(pageActivityIds)
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                ActivityParticipantRepository
                                                        .ActivityAttendanceCountProjection
                                                        ::getActivityId,
                                                projection -> projection
                                        )
                                );

        Set<Long> ownBranchIdsForMapping = ownBranchIdsForScope;
        Map<Long, ActivityInvitedBranch> invitedBranchByActivityIdForMapping =
                invitedBranchByActivityId;
        List<ActivityListItemResponse> content =
                activityPage.getContent()
                        .stream()
                        .map(activity -> {
                            ActivityListItemResponse item =
                                    activityMapper.toListItemResponse(activity);

                            populateBranchName(item, activity.getBranchId());
                            populateProvinceName(item, activity.getProvinceId());

                            if (ownBranchIdsForMapping != null) {
                                boolean isOwn =
                                        ownBranchIdsForMapping.contains(
                                                activity.getBranchId()
                                        );

                                item.setOwnBranch(isOwn);

                                if (!isOwn
                                        && invitedBranchByActivityIdForMapping
                                        != null) {

                                    ActivityInvitedBranch invitation =
                                            invitedBranchByActivityIdForMapping
                                                    .get(activity.getId());

                                    if (invitation != null) {
                                        item.setInvitationId(
                                                invitation.getId()
                                        );
                                        item.setInvitedBranchId(
                                                invitation.getBranch().getId()
                                        );
                                        item.setInvitationStatus(
                                                invitation
                                                        .getInvitationStatus()
                                        );
                                    }
                                }
                            }

                            item.setParticipantCount(
                                    participantCountsByActivityId
                                            .getOrDefault(
                                                    activity.getId(),
                                                    0L
                                            )
                            );

                            ActivityParticipantRepository.ActivityAttendanceCountProjection
                                    attendanceCounts = attendanceCountsByActivityId
                                            .get(activity.getId());
                            item.setJoinedCount(
                                    attendanceCounts == null
                                            ? 0L
                                            : attendanceCounts.getJoinedCount()
                            );
                            item.setInvitedCount(
                                    attendanceCounts == null
                                            ? 0L
                                            : attendanceCounts.getInvitedCount()
                            );

                            return item;
                        })
                        .toList();

        return ActivityPageResponse.builder()
                .content(content)
                .page(activityPage.getNumber())
                .size(activityPage.getSize())
                .totalElements(activityPage.getTotalElements())
                .totalPages(activityPage.getTotalPages())
                .first(activityPage.isFirst())
                .last(activityPage.isLast())
                .invitedActivityCount(invitedActivityCountForResponse)
                .build();
    }

    /**
     * Guards the explicit {@code branchId} list filter. A branch leader or
     * secretary may only request a branch they are staff of (same
     * branch_staff-with-home-branch-fallback set as {@link
     * #resolveStaffBranchIds}, matching {@code /api/lookups/branches}).
     * Admins and other roles are not restricted here — this endpoint is
     * read-only and callers already gate who may reach it.
     */
    private void validateExplicitBranchAccess(
            User user,
            Long branchId
    ) {
        if (user.getRole() != UserRole.BRANCH_LEADER
                && user.getRole() != UserRole.SECRETARY) {
            return;
        }

        if (!resolveStaffBranchIds(user).contains(branchId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this branch"
            );
        }
    }

    /**
     * Resolves the branches a SECRETARY or BRANCH_LEADER caller is own-branch
     * staff of (active branch_staff assignments plus their member record's
     * home branch) -- same shape as {@link #resolveStaffBranchIds}. Named
     * generically (was {@code resolveSecretaryBranchIds}) since both roles
     * use it identically here.
     */
    private Set<Long> resolveOwnBranchIds(User user) {
        return staffBranchScopeService.staffBranchIds(user);
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(
            Long activityId,
            UpdateActivityRequest request,
            Long currentUserId
    ) {
        validateUpdateRequest(request);

        Activity activity = getActivity(activityId);

        validateUpdatePermission(
                activity,
                currentUserId
        );

        ActivityType activityType =
                getActiveActivityType(request.getTypeId());

        ActivitySector activitySector =
                getActiveActivitySector(request.getSectorId());

        ActivityStatus activityStatus =
                getActiveActivityStatus(request.getStatusId());

        validateStatusForUpdate(
                activityStatus,
                request
        );

        activity.setTitleKm(
                request.getTitleKm().trim()
        );

        activity.setTitleEn(
                trimToNull(request.getTitleEn())
        );

        activity.setDescription(
                trimToNull(request.getDescription())
        );

        activity.setType(activityType);
        activity.setSector(activitySector);
        activity.setStatus(activityStatus);

        activity.setBranchId(
                request.getBranchId()
        );

        /*
         * Public visibility cannot be controlled directly by the request.
         * Visibility is resolved later from branch and invitation rules.
         */
        activity.setPublicActivity(false);

        activity.setStartsAt(
                request.getStartsAt()
        );

        activity.setEndsAt(
                request.getEndsAt()
        );

        activity.setProvinceId(
                request.getProvinceId()
        );

        activity.setDistrictId(
                request.getDistrictId()
        );

        activity.setCommuneId(
                request.getCommuneId()
        );

        activity.setLocationName(
                trimToNull(request.getLocationName())
        );

        activity.setAddress(
                trimToNull(request.getAddress())
        );

        activity.setGoogleMapUrl(
                trimToNull(request.getGoogleMapUrl())
        );

        activity.setCapacity(
                request.getCapacity()
        );

        activity.setCoverImageId(
                request.getCoverImageId()
        );

        Activity updatedActivity =
                activityRepository.save(activity);

        if (activityStatus.getCode() != null
                && "CANCELLED".equalsIgnoreCase(
                        activityStatus.getCode()
                )) {

            declineStalePendingInvitations(updatedActivity);
        }

        ActivityResponse response = activityMapper.toResponse(updatedActivity);
        populateBranchName(response, updatedActivity.getBranchId());
        return response;
    }

    @Override
    @Transactional
    public ActivityResponse completeActivity(
            Long activityId,
            Long currentUserId
    ) {
        Activity activity = getActivity(activityId);

        validateUpdatePermission(
                activity,
                currentUserId
        );

        ActivityStatus completedStatus =
                activityStatusRepository
                        .findByCodeIgnoreCase("COMPLETED")
                        .filter(status ->
                                Boolean.TRUE.equals(status.getActive())
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "COMPLETED activity status is not configured"
                                )
                        );

        activity.setStatus(completedStatus);

        Activity savedActivity =
                activityRepository.save(activity);

        declineStalePendingInvitations(savedActivity);

        return activityMapper.toResponse(
                savedActivity
        );
    }

    /**
     * Auto-declines every still-PENDING branch invitation once an activity
     * concludes (COMPLETED or CANCELLED) — accepting an invitation to an
     * activity that already happened or was called off makes no sense, and
     * leaving it PENDING would keep showing an actionable Accept/Decline
     * control (both on the activity list and the invited branch's detail
     * page banner) for something the invited branch can no longer act on.
     */
    private void declineStalePendingInvitations(
            Activity activity
    ) {
        List<ActivityInvitedBranch> pendingInvitations =
                activityInvitedBranchRepository
                        .findAllByActivity_IdAndInvitationStatus(
                                activity.getId(),
                                ActivityInvitationStatus.PENDING
                        );

        if (pendingInvitations.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        for (ActivityInvitedBranch invitation : pendingInvitations) {
            invitation.setInvitationStatus(
                    ActivityInvitationStatus.DECLINED
            );

            invitation.setRespondedAt(now);
        }

        activityInvitedBranchRepository.saveAll(
                pendingInvitations
        );
    }

    private Activity getActivity(
            Long activityId
    ) {
        if (activityId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity id is required"
            );
        }

        return activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Activity not found with id: "
                                        + activityId
                        )
                );
    }

    private ActivityType getActiveActivityType(
            Short typeId
    ) {
        if (typeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity type is required"
            );
        }

        return activityTypeRepository.findById(typeId)
                .filter(type ->
                        Boolean.TRUE.equals(
                                type.getActive()
                        )
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Activity type is invalid or inactive"
                        )
                );
    }

    private ActivitySector getActiveActivitySector(
            Short sectorId
    ) {
        if (sectorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity sector is required"
            );
        }

        return activitySectorRepository.findById(sectorId)
                .filter(sector ->
                        Boolean.TRUE.equals(
                                sector.getActive()
                        )
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Activity sector is invalid or inactive"
                        )
                );
    }

    private ActivityStatus getActiveActivityStatus(
            Short statusId
    ) {
        if (statusId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity status is required"
            );
        }

        return activityStatusRepository.findById(statusId)
                .filter(status ->
                        Boolean.TRUE.equals(
                                status.getActive()
                        )
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Activity status is invalid or inactive"
                        )
                );
    }

    private ActivityStatus resolveInitialStatus(
            CreateActivityRequest request
    ) {
        ActivityStatus requestedStatus =
                getActiveActivityStatus(
                        request.getStatusId()
                );

        String statusCode =
                requestedStatus.getCode();

        OffsetDateTime now =
                OffsetDateTime.now();

        /*
         * Draft activities stay draft regardless of their dates.
         */
        if ("DRAFT".equalsIgnoreCase(statusCode)) {
            return requestedStatus;
        }

        /*
         * Completion must happen through the manual completion endpoint.
         */
        if ("COMPLETED".equalsIgnoreCase(statusCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity cannot be created as completed"
            );
        }

        /*
         * If the activity was submitted as UPCOMING but its start time
         * has already arrived, save it as ONGOING.
         */
        if ("UPCOMING".equalsIgnoreCase(statusCode)
                && !request.getStartsAt()
                .isAfter(now)) {

            return activityStatusRepository
                    .findByCodeIgnoreCase("ONGOING")
                    .filter(status ->
                            Boolean.TRUE.equals(
                                    status.getActive()
                            )
                    )
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "ONGOING activity status is not configured"
                            )
                    );
        }

        /*
         * An activity scheduled for the future cannot begin as ONGOING.
         */
        if ("ONGOING".equalsIgnoreCase(statusCode)
                && request.getStartsAt()
                .isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A future activity cannot be created as ongoing"
            );
        }

        return requestedStatus;
    }

    private void validateStatusForUpdate(
            ActivityStatus requestedStatus,
            UpdateActivityRequest request
    ) {
        String statusCode =
                requestedStatus.getCode();

        OffsetDateTime now =
                OffsetDateTime.now();

        /*
         * COMPLETED is handled through a separate manual endpoint.
         */
        if ("COMPLETED".equalsIgnoreCase(statusCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the complete activity endpoint to mark the activity as completed"
            );
        }

        /*
         * An activity whose start time is still in the future
         * cannot be marked ONGOING.
         */
        if ("ONGOING".equalsIgnoreCase(statusCode)
                && request.getStartsAt()
                .isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A future activity cannot be marked as ongoing"
            );
        }
    }

    /**
     * Only a branch leader or secretary who is staff of THIS activity's own
     * branch may modify it — not the original creator per se, not an admin,
     * and not staff of a different branch. Replaces the previous
     * creator-only check, which allowed anyone who happened to be
     * {@code activity.createdBy} to edit regardless of role or branch.
     */
    private void validateUpdatePermission(
            Activity activity,
            Long currentUserId
    ) {
        User currentUser = requireUser(currentUserId);

        if (!computeCanManage(currentUser, activity)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Branch Leader or Secretary staff of this "
                            + "activity's own branch can modify it"
            );
        }
    }

    /**
     * Administrators manage activities organization-wide. Branch leaders
     * and secretaries may manage activities hosted by one of their branches.
     * Members and viewers never manage activities.
     */
    private boolean computeCanManage(
            User user,
            Activity activity
    ) {
        // ADMIN is organization-wide read-only for activities.
        if (user.getRole() != UserRole.BRANCH_LEADER
                && user.getRole() != UserRole.SECRETARY) {
            return false;
        }

        if (activity.getBranchId() == null) {
            return false;
        }

        return resolveStaffBranchIds(user)
                .contains(activity.getBranchId());
    }

    /**
     * If this user is a branch leader/secretary of a branch that has an
     * ACCEPTED invitation to co-host this activity, returns that branch's
     * id — otherwise {@code null}. Only meaningful when the caller is NOT
     * already {@link #computeCanManage}; the host branch never needs this.
     */
    private Long resolveManagedInvitedBranchId(
            User user,
            Activity activity
    ) {
        if (user.getRole() != UserRole.BRANCH_LEADER
                && user.getRole() != UserRole.SECRETARY) {
            return null;
        }

        for (Long staffBranchId : resolveStaffBranchIds(user)) {
            boolean accepted = activityInvitedBranchRepository
                    .findByActivity_IdAndBranch_IdAndInvitationStatus(
                            activity.getId(),
                            staffBranchId,
                            ActivityInvitationStatus.ACCEPTED
                    )
                    .isPresent();

            if (accepted) {
                return staffBranchId;
            }
        }

        return null;
    }

    /**
     * Branch(es) a branch-leader/secretary user is staff of — via an active
     * branch_staff assignment, or their own member record's home branch.
     * Deliberately role-agnostic (unlike {@link #resolveSecretaryBranchIds},
     * which is kept as-is for the existing SECRETARY-only list scoping).
     */
    private Set<Long> resolveStaffBranchIds(
            User user
    ) {
        return staffBranchScopeService.staffBranchIds(user);
    }

    private User requireUser(
            Long userId
    ) {
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user could not be found"
                        )
                );
    }

    /**
     * Resolves {@code activity.createdBy} (a {@code users.id}) to the
     * creator's display name and phone via that user's linked member
     * record, and sets them on the response. Previously the response only
     * ever carried the raw {@code createdBy} id, so the activity detail
     * page's "អ្នកគ្រប់គ្រង"/"លេខទំនាក់ទំនង" fields fell all the way
     * through to showing "#{id}" and "-" respectively. Best-effort: a
     * creator account with no linked member (or a since-deleted user)
     * just leaves these null, same as before.
     */
    private void resolveCreatedByDisplay(
            Activity activity,
            ActivityResponse response
    ) {
        if (activity.getCreatedBy() == null) {
            return;
        }

        userRepository.findById(activity.getCreatedBy())
                .map(User::getMemberId)
                .filter(memberId -> memberId != null)
                .flatMap(memberRepository::findById)
                .ifPresent(creatorMember -> {
                    String name = creatorMember.getFullNameKm();

                    if (name == null || name.isBlank()) {
                        name = creatorMember.getFullNameEn();
                    }

                    response.setCreatedByName(name);
                    response.setCreatedByPhone(creatorMember.getPhone());
                });
    }

    private void validateCreateRequest(
            CreateActivityRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity request is required"
            );
        }

        validateCommonRequestFields(
                request.getTitleKm(),
                request.getTitleEn(),
                request.getStartsAt(),
                request.getEndsAt(),
                request.getProvinceId(),
                request.getDistrictId(),
                request.getCommuneId(),
                request.getCapacity(),
                request.getBranchId()
        );
    }

    private void validateUpdateRequest(
            UpdateActivityRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity request is required"
            );
        }

        validateCommonRequestFields(
                request.getTitleKm(),
                request.getTitleEn(),
                request.getStartsAt(),
                request.getEndsAt(),
                request.getProvinceId(),
                request.getDistrictId(),
                request.getCommuneId(),
                request.getCapacity(),
                request.getBranchId()
        );
    }

    private void validateCommonRequestFields(
            String titleKm,
            String titleEn,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            Short provinceId,
            Integer districtId,
            Integer communeId,
            Integer capacity,
            Long branchId
    )  {
        if (titleKm == null
                || titleKm.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khmer activity title is required"
            );
        }

        if (currentStringLength(titleKm) > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khmer activity title is too long"
            );
        }

        if (currentStringLength(titleEn) > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "English activity title is too long"
            );
        }

        if (startsAt == null || endsAt == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity start and end times are required"
            );
        }

        if (!endsAt.isAfter(startsAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity end time must be later than start time"
            );
        }

        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity branch is required"
            );
        }

        if (communeId != null && districtId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "District is required when commune is selected"
            );
        }

        if (districtId != null && provinceId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Province is required when district is selected"
            );
        }

        if (capacity != null && capacity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity capacity must be greater than zero"
            );
        }
    }

    private int currentStringLength(
            String value
    ) {
        return value == null
                ? 0
                : value.trim().length();
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
    private void populateBranchName(
            ActivityResponse response,
            Long branchId
    ) {
        if (response == null || branchId == null) {
            return;
        }

        branchRepository.findById(branchId).ifPresent(branch -> {
            response.setBranchNameKm(branch.getNameKm());
            response.setBranchNameEn(branch.getNameEn());
        });
    }

    private void populateBranchName(
            ActivityListItemResponse response,
            Long branchId
    ) {
        if (response == null || branchId == null) {
            return;
        }

        branchRepository.findById(branchId).ifPresent(branch -> {
            response.setBranchNameKm(branch.getNameKm());
            response.setBranchNameEn(branch.getNameEn());
        });
    }

    private void populateProvinceName(
            ActivityListItemResponse response,
            Short provinceId
    ) {
        if (response == null || provinceId == null) {
            return;
        }

        provinceRepository.findById(provinceId).ifPresent(province -> {
            response.setProvinceNameKm(province.getNameKm());
            response.setProvinceNameEn(province.getNameEn());
        });
    }

}
