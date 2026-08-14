package org.example.tnal_youth_backend.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.mapper.ActivityMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
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
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivitySectorRepository activitySectorRepository;
    private final ActivityStatusRepository activityStatusRepository;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;
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

        return activityMapper.toResponse(savedActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(
            Long activityId,
            Long currentUserId
    ) {
        Activity activity = getActivity(activityId);

        User currentUser = requireUser(currentUserId);

        /*
         * A member may only view the detail of an activity they were
         * personally invited to (i.e. they are already a participant).
         * Staff roles (admin, secretary, branch leader) can view any
         * activity.
         */
        if (currentUser.getRole() == UserRole.MEMBER) {
            boolean invited =
                    currentUser.getMemberId() != null
                            && activityParticipantRepository
                            .existsByActivity_IdAndMember_Id(
                                    activityId,
                                    currentUser.getMemberId()
                            );

            if (!invited) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not invited to this activity"
                );
            }
        }

        ActivityResponse response =
                activityMapper.toResponse(activity);

        boolean canManage = computeCanManage(currentUser, activity);
        response.setCanManage(canManage);

        if (!canManage) {
            Long invitedBranchId =
                    resolveManagedInvitedBranchId(currentUser, activity);

            response.setCanManageAsInvitedBranch(
                    invitedBranchId != null
            );
            response.setManagedInvitedBranchId(invitedBranchId);
        } else {
            response.setCanManageAsInvitedBranch(false);
        }

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
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user could not be found"
                ));

        if (currentUser.getRole() == UserRole.SECRETARY) {
            Set<Long> branchIds = resolveSecretaryBranchIds(currentUser);
            activityPage = activityRepository.findAllByBranchIdIn(
                    branchIds,
                    pageable
            );
        } else if (currentUser.getRole() == UserRole.MEMBER) {
            /*
             * A member only sees activities they were personally invited
             * to — never the full activity catalog.
             */
            List<Long> invitedActivityIds =
                    currentUser.getMemberId() == null
                            ? List.of()
                            : activityParticipantRepository
                            .findDistinctActivityIdsByMemberId(
                                    currentUser.getMemberId()
                            );

            activityPage = invitedActivityIds.isEmpty()
                    ? Page.empty(pageable)
                    : activityRepository.findAllByIdIn(
                            invitedActivityIds,
                            pageable
                    );
        } else {
            activityPage = activityRepository.findAll(pageable);
        }

        List<ActivityListItemResponse> content =
                activityPage.getContent()
                        .stream()
                        .map(activityMapper::toListItemResponse)
                        .toList();

        return ActivityPageResponse.builder()
                .content(content)
                .page(activityPage.getNumber())
                .size(activityPage.getSize())
                .totalElements(activityPage.getTotalElements())
                .totalPages(activityPage.getTotalPages())
                .first(activityPage.isFirst())
                .last(activityPage.isLast())
                .build();
    }

    private Set<Long> resolveSecretaryBranchIds(User user) {
        if (user.getMemberId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Secretary account is not linked to a member"
            );
        }

        Member member = memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Secretary member record could not be found"
                ));

        Set<Long> branchIds = new LinkedHashSet<>(
                branchStaffRepository.findActiveBranchIdsByMemberId(
                        user.getMemberId()
                )
        );

        if (member.getBranchId() != null) {
            branchIds.add(member.getBranchId());
        }

        if (branchIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Secretary is not assigned to a branch"
            );
        }

        return Set.copyOf(branchIds);
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

        return activityMapper.toResponse(updatedActivity);
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

        return activityMapper.toResponse(
                activityRepository.save(activity)
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
                    "Only a branch leader or secretary of this "
                            + "activity's own branch can modify it"
            );
        }
    }

    /**
     * True only for a branch leader or secretary who is staff of this
     * activity's own branch. Admins and members always get {@code false} —
     * admins are limited to viewing the activity module, and members never
     * manage activities at all.
     */
    private boolean computeCanManage(
            User user,
            Activity activity
    ) {
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
        if (user.getMemberId() == null) {
            return Set.of();
        }

        Set<Long> branchIds = new LinkedHashSet<>(
                branchStaffRepository.findActiveBranchIdsByMemberId(
                        user.getMemberId()
                )
        );

        memberRepository.findById(user.getMemberId())
                .map(Member::getBranchId)
                .ifPresent(branchIds::add);

        return branchIds;
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
}
